package com.faisalaffan.javara.ofs;

public record OFSEnvelope(String xml) {
    public OFSEnvelope {
        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException("OFS XML must not be blank");
        }
    }
}
