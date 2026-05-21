package com.pca.Backend.Utils;

import java.util.UUID;

public class Utils {
   public static String generateCorrelationId(){
              return UUID.randomUUID().toString().substring(0, 8);
    }

}
