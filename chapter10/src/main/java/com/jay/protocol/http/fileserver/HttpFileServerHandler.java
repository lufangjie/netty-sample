package com.jay.protocol.http.fileserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;


/**
 * @ClassName HttpFileServerHandler
 * @Description Http文件服务器处理类
 * @Date 2019/3/11
 * @Author lufangjie
 * @Version 1.0
 **/
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private String fileUrl;
    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    public HttpFileServerHandler(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 对HTTP请求消息进行解码
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        // 对HTTP请求行中的方法进行判断
        if (request.method() != HttpMethod.GET) {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        // 格式化文件路径
        final String uri = request.uri();
        final String path = sanitizeUri(uri);
        if (path == null) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        if (file.isDirectory()) {
            if (uri.endsWith("/")) {
                sendFileList(ctx, file);
            } else {
                sendRedirect(ctx, uri + '/');
            }
            return;
        }

        if (!file.isFile()) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        RandomAccessFile randomAccessFile = null;

        try {
            // 以只读模式打开
            randomAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        long fileLength = randomAccessFile.length();
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        HttpUtil.setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(response);
        ChannelFuture sendFileFuture;

        sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
                if (total < 0) {
                    System.err.println("Transfer progress:" + progress);
                } else {
                    System.err.println("Transfer progress:" + progress + "/" + total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                System.out.println("Transfer complete.");
            }
        });
        // 如果使用chunked编码，最后需要发送一个编码结束的空消息体，将LastHttpContent.EMPTY_LAST_CONTENT发送到缓冲区中，标识所有的消息已经发送完成
        // 同时调用flush方法将之前在缓冲区的消息刷新到SocketChannel中发送给对方
        // 如果是非Keep-Alive状态，最后一包消息发送完完成之后，服务端要主动关闭连接
        ChannelFuture lastFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        // 如果当前连接请求非Keep-Alive ，最后一包消息发送完成后 服务器主动关闭连接
        if (!HttpUtil.isKeepAlive(request)) {
            lastFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String sanitizeUri(String uri) {
        // 对URL进行解码
        try {
            uri = URLDecoder.decode(uri, CharsetUtil.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            try {
                uri = URLDecoder.decode(uri, CharsetUtil.ISO_8859_1.toString());
            } catch (UnsupportedEncodingException ex) {
                throw new Error();
            }
        }

        // 判断URL合法性
        if (!uri.startsWith(fileUrl)) {
            return null;
        }
        if (!uri.startsWith("/")) {
            return null;
        }

        // 将硬编码的文件路径分隔符替换成本地操作的文件路径分隔符
        uri = uri.replace('/', File.separatorChar);
        // 二次校验
        if (uri.contains(File.separator + '.')
                || uri.contains('.' + File.separator) || uri.startsWith(".")
                || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
            return null;
        }
        String userDir = "D:\\文档" + uri;
        return userDir;
    }

    /** 允许显示的文件名称 */
    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    private static void sendFileList(ChannelHandlerContext ctx, File dir) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append(" 目录：");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }
            buf.append("<li>链接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        buf.append("</ul></body></html>\r\n");
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer);
        buffer.release();
        // 使用ctx对象写出并且刷新到SocketChannel中去 并主动关闭连接(这里是指关闭处理发送数据的线程连接)
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 重定向操作
     * @param ctx
     * @param newUri 新的uri
     *
     **/
    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        // 建立响应对象
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        // 设置新的请求地址放入响应对象中去
        response.headers().set(HttpHeaderNames.LOCATION, newUri);
        // 使用ctx对象写出并且刷新到SocketChannel中去 并主动关闭连接(这里是指关闭处理发送数据的线程连接)
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure:" + status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setContentTypeHeader(HttpResponse response, File file) {
        // 使用mime对象获取文件类型
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                mimeTypesMap.getContentType(file.getPath()));
    }
}
