package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class VietQRService {

    private static final String FILE_PATH = "json/bankAccount.json";
    private final BankAccountConfig config;

    public static class BankAccountConfig {
        public String bankShortName;
        public String accountNumber;
        public String accountName;
        public String template;
    }

    public VietQRService() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            config = mapper.readValue(new File(FILE_PATH), BankAccountConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load bank account config", e);
        }
    }

    public String generateQRCodeUrl(double amount, String paymentDescription) {
        try {
            String encodedDesc = java.net.URLEncoder.encode(paymentDescription, "UTF-8");
            String encodedName = java.net.URLEncoder.encode(config.accountName, "UTF-8");

            // Format amounts properly (no decimal points if 0)
            long roundAmount = (long) amount;

            return String.format("https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                    config.bankShortName,
                    config.accountNumber,
                    config.template,
                    roundAmount,
                    encodedDesc,
                    encodedName
            );
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
