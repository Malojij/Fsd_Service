package TransactionId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;

public class TransactionId {

    // Lookup table for Node -> Environment
    private static final Map<Integer, String> ENV_MAP = Map.of(
            91, "UAT: APP1",
            92, "UAT: APP2",
            93, "UAT: APP3",
            94, "UAT: APP4",
            95, "SGND",
            96, "LDND",
            97, "QA42"
    );

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Transaction ID: ");
        String txnId = sc.nextLine();

        try {

            TransactionDetails result = parseTransaction(txnId);

            String zipDate = result.zipDate();
            int hour = Integer.parseInt(zipDate.substring(zipDate.length() - 2));

            System.out.println("\nNext ZIP Hour : "
                    + zipDate.substring(0, zipDate.length() - 2)
                    + String.format("%02d", hour + 1));

            System.out.println("\n========== Transaction Details ==========");
            System.out.println("Transaction ID : " + result.transactionId());
            System.out.println("TXN Length     : " + result.txnLength());
            System.out.println("Node-ID        : " + result.nodeId());
            System.out.println("Environment    : " + result.environment());
            System.out.println("DateTime       : " + result.dateTime());
            System.out.println("ZIPDate        : " + result.zipDate());

        } catch (IllegalArgumentException e) {
            System.out.println("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected Error: " + e.getMessage());
        } finally {
            sc.close();
        }
    }

    public static TransactionDetails parseTransaction(String txnId) {

        validate(txnId);

        txnId = txnId.trim();

        int offset = (txnId.length() == 18) ? 0 : 1;

        String nodeId = txnId.substring(1, 3 + offset);

        int node = Integer.parseInt(offset == 0 ? nodeId : nodeId.substring(0, 2));

        int year = Integer.parseInt("20" +txnId.substring(3 + offset, 5 + offset));

        int day = Integer.parseInt(txnId.substring(5 + offset, 8 + offset));

        LocalDate date = LocalDate.ofYearDay(year, day);

        String formattedDate =date.format(DateTimeFormatter.ISO_DATE);

        long millis = Long.parseLong(txnId.substring(8 + offset, 16 + offset));

        long totalSeconds = millis / 1000;

        String time = String.format("%02d:%02d:%02d",
                totalSeconds / 3600,
                (totalSeconds % 3600) / 60,
                totalSeconds % 60);

        String zipDate = formattedDate + "-" + time.substring(0, 2);

        return new TransactionDetails(
                txnId,
                txnId.length(),
                nodeId,
                ENV_MAP.getOrDefault(node, "PROD"),
                formattedDate + " " + time,
                zipDate
        );
    }

    private static void validate(String txnId) {

        if (txnId == null || txnId.isBlank()) {
            throw new IllegalArgumentException("Transaction ID cannot be empty.");
        }

        if (txnId.length() != 18 && txnId.length() != 19) {
            throw new IllegalArgumentException("Invalid Transaction ID length. Expected 18 or 19 characters.");
        }

        if (!txnId.matches("\\d+")) {
            throw new IllegalArgumentException("Transaction ID must contain only digits.");
        }
    }
}

/*
 * Record to hold parsed transaction details.
 */
record TransactionDetails(
        String transactionId,
        int txnLength,
        String nodeId,
        String environment,
        String dateTime,
        String zipDate
) {
}