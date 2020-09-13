package com.mokrousov.security.vigenere;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
  public static final double[] FREQUENCIES = {
    0.08167, 0.01492, 0.02782, 0.04253, 0.12702, 0.02228, 0.02015,
    0.06094, 0.06966, 0.00153, 0.00772, 0.04025, 0.02406, 0.06749,
    0.07507, 0.01929, 0.00095, 0.05987, 0.06327, 0.09056, 0.02758,
    0.00978, 0.02360, 0.00150, 0.01974, 0.00074
  };
  
  public static String encode(String text, String key) {
    int keyLen = key.length();
    StringBuilder sb = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      int c = text.charAt(i) - 'a';
      int offset = key.charAt(i % keyLen) - 'a';
      sb.append((char) ((c + offset) % 26 + 'a'));
    }
    
    return sb.toString();
  }
  
  public static String decode(String text, String key) {
    int keyLen = key.length();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      int c = text.charAt(i) - 'a';
      int offset = key.charAt(i % keyLen) - 'a';
      sb.append((char) ((c - offset + 26) % 26 + 'a'));
    }
    
    return sb.toString();
  }
  
  public static String preprocess(String text) {
    return text.replaceAll("[^a-zA-Z]", "").toLowerCase();
  }
  
  // go over each possible key length k (does not make sense for big lengths)
  // that means that input string will be split in k different character subsequences
  // each subsequence has it's own distribution
  // for each of the subsequences compute the character distribution
  // then find the index of coincidence
  // take the average over all subsequences
  // find the first one, which has the index >= 0.06 (this may be incorrect sometimes)
  // finally return the length
  public static int findKeyLength(String text) {
    for (int len = 1; len <= Math.min(text.length(), 25); len++) {
      double averageIndex = 0;
      for (int sequence = 1; sequence <= len; sequence++) {
        int[] distribution = new int[26];
        double indexOfCoincidence = 0;
        int count = 0;
        
        for (int k = sequence - 1; k < text.length(); k += len) {
          distribution[text.charAt(k) - 'a']++;
          count++;
        }
        
        for (double frequency : distribution) {
          indexOfCoincidence += frequency * (frequency - 1);
        }
        
        indexOfCoincidence /= (count * (count - 1));
        averageIndex += indexOfCoincidence;
      }
      
      averageIndex /= len;
      if (averageIndex >= 0.06) {
        return len;
      }
    }
    
    return -1;
  }
  
  public static double findCorrelation(double[] a, double[] b) {
    double correlation = 0;
    for (int i = 0; i < a.length; i++)
      correlation += a[i] * b[i];
    return correlation;
  }
  
  // find length of key using coincidence index
  // find the mapping using frequency analysis
  // return decoded key
  public static String decrypt(String text) {
    int len = findKeyLength(text);
    StringBuilder key = new StringBuilder();
  
    // for each letter find new distribution
    // find the correlation of distibution
    // save max value for given character
    // append the character to the given key
    // return decoded text
    for (int sequence = 1; sequence <= len; sequence++) {
      double maxCorrelation = 0;
      char maxChar = 'a';
      for (int i = 0; i < 26; i++) {
        char currChar = (char) (i + 'a');
        int count = 0;
        
        double[] distribution = new double[26];
        for (int k = sequence - 1; k < text.length(); k += len) {
          int c = (text.charAt(k) - 'a' - i + 26) % 26;
          distribution[c]++;
          count++;
        }
        
        for (int k = 0; k < 26; k++) {
          distribution[k] /= count;
        }
        
        double correlation = findCorrelation(distribution, FREQUENCIES);
        if (correlation > maxCorrelation) {
          maxCorrelation = correlation;
          maxChar = currChar;
        }
      }
      
      key.append(maxChar);
    }
    
    return decode(text, key.toString());
  }
  
  public static void main(String[] args) throws IOException {
    String key = "abc";
    String text = preprocess(Files.readString(Path.of("resources/small.txt")));
    String encoded = encode(text, key);
    String decoded = decrypt(encoded);
    
    System.out.println("Key: " + key);
    System.out.println("Text: " + text);
    System.out.println("Encoded: " + encoded);
    System.out.println("Decoded: " + decoded);
    System.out.println("Decrypted correctly: " + text.equals(decoded));
  }
}
