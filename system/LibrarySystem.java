package system;

import db.DBConnection;
import model.*;
import exceptions.*;
import java.sql.*;

public class LibrarySystem {

    public void authorized(int id) throws UnauthorisedException, SQLException {
        Connection conn = DBConnection.getConnection();

        String query = "SELECT isStudent FROM Users WHERE UserID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean isStudent = rs.getBoolean("isStudent");
                if (isStudent) {
                    throw new UnauthorisedException("Not a librarian.");
                }
                // Authorized librarian, do nothing
            } else {
                throw new UnauthorisedException("User not found.");
            }
        }
        conn.close();
        System.out.println("Is a Librarian. Proceed");

    }

    public void addBook(Book book) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String query = "INSERT INTO Books(title, author, copies, availableCopies) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, book.getTitle());
        stmt.setString(2, book.getAuthor());
        stmt.setInt(3, book.getTotalCopies());
        stmt.setInt(4, book.getAvailableCopies());
        stmt.executeUpdate();
        conn.close();
        System.out.println("Book added successfully!");
    }

    public void registerUser(User user) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String query = "INSERT INTO Users(UserName, isStudent, mobile) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, user.getName());
        stmt.setBoolean(2, user.isStudent());
        stmt.setLong(3, user.getMobile());
        stmt.executeUpdate();
        conn.close();
        System.out.println("User Registered Successfully!");
    }

    public void borrowBook(int userId, int bookId)
            throws SQLException, InvalidUserException, InvalidBookException, BookNotAvailableException {
        Connection conn = DBConnection.getConnection();

        // Check user
        PreparedStatement userStmt = conn.prepareStatement("SELECT * FROM Users WHERE UserID = ?");
        userStmt.setInt(1, userId);
        ResultSet userRs = userStmt.executeQuery();
        if (!userRs.next())
            throw new InvalidUserException("User not found!");

        // Check book
        PreparedStatement bookStmt = conn.prepareStatement("SELECT * FROM Books WHERE BookID = ?");
        bookStmt.setInt(1, bookId);
        ResultSet bookRs = bookStmt.executeQuery();
        if (!bookRs.next())
            throw new InvalidBookException("Book not found!");
        int available = bookRs.getInt("availableCopies");
        if (available <= 0)
            throw new BookNotAvailableException("Book not available!");

        // Update borrowedBooks
        PreparedStatement borrowStmt = conn.prepareStatement(
                "INSERT INTO borrowedBooks(UserID, BookID, borrowDate, isReturned) VALUES (?, ?, CURDATE(), false)");
        borrowStmt.setInt(1, userId);
        borrowStmt.setInt(2, bookId);
        borrowStmt.executeUpdate();

        // Update availableCopies
        PreparedStatement updateStmt = conn.prepareStatement(
                "UPDATE Books SET availableCopies = availableCopies - 1 WHERE BookID = ?");
        updateStmt.setInt(1, bookId);
        updateStmt.executeUpdate();

        System.out.println("Book burrowed successfully!");

        conn.close();
    }

    public void returnBook(int userId, int bookId) throws SQLException {
        Connection conn = DBConnection.getConnection();

        PreparedStatement returnStmt = conn.prepareStatement(
                "UPDATE borrowedBooks SET returnDate = CURDATE(), isReturned = true WHERE UserID = ? AND BookID = ? AND isReturned = false");
        returnStmt.setInt(1, userId);
        returnStmt.setInt(2, bookId);
        int rows = returnStmt.executeUpdate();

        if (rows > 0) {
            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE Books SET availableCopies = availableCopies + 1 WHERE BookID = ?");
            updateStmt.setInt(1, bookId);
            updateStmt.executeUpdate();
        }

        System.out.println("Book returned successfully!");
        conn.close();
    }

    public void listAllBooks() throws SQLException {
        Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM Books");
        while (rs.next()) {
            System.out.println("BookID: " + rs.getInt("BookID") + ", Title: " + rs.getString("title") +
                    ", Author: " + rs.getString("author") + ", Available: " + rs.getInt("availableCopies"));
        }
        conn.close();
    }

    public void searchBooksByOption(int option, String keyword) throws SQLException {
        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = null;

        switch (option) {
            case 1 -> { // Title
                stmt = conn.prepareStatement("SELECT * FROM Books WHERE title LIKE ?");
                stmt.setString(1, "%" + keyword + "%");
            }
            case 2 -> { // Author
                stmt = conn.prepareStatement("SELECT * FROM Books WHERE author LIKE ?");
                stmt.setString(1, "%" + keyword + "%");
            }
            case 3 -> { // BookID
                stmt = conn.prepareStatement("SELECT * FROM Books WHERE BookID = ?");
                stmt.setInt(1, Integer.parseInt(keyword));
            }
            default -> {
                System.out.println("Invalid option");
                conn.close();
                return;
            }
        }

        ResultSet rs = stmt.executeQuery();
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("BookID: " + rs.getInt("BookID") +
                    ", Title: " + rs.getString("title") +
                    ", Author: " + rs.getString("author") +
                    ", Available: " + rs.getInt("availableCopies"));
        }
        if (!found) {
            System.out.println("No books found.");
        }

        conn.close();
    }

    public void checkDueAndFine(int userId) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String query = "SELECT b.title, br.borrowDate, br.returnDate, br.isReturned " +
                "FROM borrowedBooks br JOIN Books b ON br.BookID = b.BookID " +
                "WHERE br.UserID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();

        boolean any = false;
        while (rs.next()) {
            any = true;
            String title = rs.getString("title");
            Date borrowDate = rs.getDate("borrowDate");
            Date returnDate = rs.getDate("returnDate");
            boolean returned = rs.getBoolean("isReturned");

            System.out.println("\nBook: " + title);
            System.out.println("Borrowed on: " + borrowDate);

            if (!returned) {
                long diff = (System.currentTimeMillis() - borrowDate.getTime()) / (1000 * 60 * 60 * 24);
                if (diff > 14) {
                    System.out.println("Overdue! Fine: ₹100");
                } else {
                    System.out.println("Due in " + (14 - diff) + " day(s)");
                }
            } else {
                System.out.println("Returned on: " + returnDate);
            }
        }

        if (!any) {
            System.out.println("No borrowed books found.");
        }

        conn.close();
    }

}
