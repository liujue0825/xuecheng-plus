package com.xuecheng.base.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件读取工具类
 */
public class FileUtil {

    private FileUtil() {

    }

    /**
     * 根据 md5 值和文件扩展名生成文件路径
     */
    public static String getFilePathByMd5(String fileMd5, String extension) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extension;
    }

    /**
     * 读取文件内容，作为字符串返回
     */
    public static String readFileAsString(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException(filePath);
        }

        long fileSize = Files.size(path);
        if (fileSize > 1024 * 1024 * 1024) {
            throw new IOException("File is too large");
        }

        // 对于小文件，直接读取全部内容
        if (fileSize < 10 * 1024 * 1024) { // 10MB
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        }

        // 对于较大的文件，使用 BufferedReader
        StringBuilder sb = new StringBuilder((int) fileSize);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            char[] buffer = new char[8192]; // 8KB buffer
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, charsRead);
            }
        }
        return sb.toString();
    }

    /**
     * 根据文件路径读取 byte[] 数组
     */
    public static byte[] readFileByBytes(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException(filePath);
        }

        try {
            return Files.readAllBytes(path);
        } catch (OutOfMemoryError e) {
            // 如果文件太大，无法一次性读入内存，则使用缓冲方式读取
            return readLargeFile(path);
        }
    }

    private static byte[] readLargeFile(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192]; // 使用 8KB 的缓冲区
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }
    }

}
