package com.neusoft.edu.neullmdev.tool.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 鑵捐浣嶇疆鏈嶅姟 WebService 绛惧悕鏍￠獙銆? */
public final class TencentMapSigUtil {

    private TencentMapSigUtil() {
    }

    public static String computeGetSig(String sk, String uriPath, Map<String, String> params) {
        if (sk == null || sk.isEmpty()) {
            throw new IllegalStateException("鏈厤缃?tencent.map.secret锛堟帶鍒跺彴 SK锛夛紝鏃犳硶璁＄畻 sig");
        }
        TreeMap<String, String> sorted = new TreeMap<>(params);
        String query = sorted.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        String plain = uriPath + "?" + query + sk;
        return md5LowerHex(plain.getBytes(StandardCharsets.UTF_8));
    }

    private static String md5LowerHex(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] d = md.digest(input);
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
