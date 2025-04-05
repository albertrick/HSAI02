package com.example.hsai02;

import static java.security.AccessController.getContext;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.media.ApplicationMediaCapabilities;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.google.ai.client.generativeai.type.Part;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FilePart implements Part {

    private final File file;
    private final String contentType;
    Context context;
    ContentResolver cr;

    public FilePart(File file) {
        this.file = file;
        context = this.context.getApplicationContext();
        cr = context.getContentResolver();
        this.contentType = cr.getType(Uri.fromFile(file));
    }

//
//    @Override
//    public InputStream getInputStream() throws IOException {
//        return new FileInputStream(file);
//    }
//
//    @Override
//    public String getContentType() {
//        return contentType;
//    }
//
//    @Override
//    public String getName() {
//        return file.getName();
//    }
//
//    @Override
//    public String getSubmittedFileName() {
//        return file.getName();
//    }
//
//    @Override
//    public long getSize() {
//        return file.length();
//    }
//
//    @Override
//    public void write(String fileName) throws IOException {
//        Files.copy(file.toPath(), Paths.get(fileName));
//    }
//
//    @Override
//    public void delete() throws IOException {
//        file.delete();
//    }
//
//    // Remaining methods (getHeader, getHeaders, getHeaderNames, etc.)
//    // can be implemented as needed, or left empty for simplicity if not required.
//
//    @Override
//    public String getHeader(String name) {
//        return null;
//    }
//
//    @Override
//    public java.util.Collection<String> getHeaders(String name) {
//        return null;
//    }
//
//    @Override
//    public java.util.Collection<String> getHeaderNames() {
//        return null;
//    }
//
//    @Override
//    public void setHeader(String name, String value) {
//
//    }
//
//    @Override
//    public void addHeader(String name, String value) {
//
//    }
//    @Override
//    public void write(OutputStream out) throws IOException{
//        Files.copy(file.toPath(), out);
//    }
}
