package com.example.myapplication;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

public class BufferHttpServer extends NanoHTTPD {
    private byte[] audioBuffer;
    public BufferHttpServer(int port, byte[] audioBuffer){
        super(port);
        this.audioBuffer = audioBuffer;
    }

    @Override
    public Response serve(IHTTPSession session) {
        InputStream inputStream = new ByteArrayInputStream(audioBuffer);
        return newFixedLengthResponse(Response.Status.OK, "audio/3gpp", inputStream, audioBuffer.length);
    }

}
