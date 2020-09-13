package com.mokrousov.security.vigenere;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
  public static String encode(String text, String key) {
    int keyLen = key.length();
    key = preprocess(key);
    text = text.toLowerCase();
    StringBuilder sb = new StringBuilder(text.length());
    int j = 0;
    for (int i = 0; i < text.length(); i++) {
      if (!Character.isLetter(text.charAt(i))) {
        sb.append(text.charAt(i));
        continue;
      }

      int c = text.charAt(i) - 'a';
      int offset = key.charAt((j++) % keyLen) - 'a';
      sb.append((char) ((c + offset) % 26 + 'a'));
    }
    
    return sb.toString();
  }
  
  public static String preprocess(String text) {
    return text.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase();
  }
  
  public static String decode(String text) {
    return null;
  }
  
  // TODO: uppercase letters
  public static void main(String[] args) throws IOException {
    String key = "abc";
    String text = Files.readString(Path.of("resources/encoded.txt")).replaceAll("\n", "").toLowerCase();
    String encoded = encode(text, key);
    String decoded = decode(encoded);
    System.out.println("Decrypted correctly: " + text.equals(decoded));
  }
}
