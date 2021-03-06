package fatec.com.digital_library.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fatec.com.digital_library.dao.BookAutorDAO;
import fatec.com.digital_library.dao.BookCategoryDAO;
import fatec.com.digital_library.dao.BookDAO;
import fatec.com.digital_library.entity.Autor;
import fatec.com.digital_library.entity.Book;
import fatec.com.digital_library.entity.Category;
import fatec.com.digital_library.entity.Editor;
import fatec.com.digital_library.utility.DatabaseConnection;

public class BookDAOImpl implements BookDAO {

	private String query;
	private String dml;
	private StringBuilder builder;
	private BookAutorDAO bookAutorDAO;
	private BookCategoryDAO bookCategoryDAO;
	
	@Override
	public boolean addBook(Book book) {
		DatabaseConnection dbCon;
		PreparedStatement ps;
		Connection con;
		dbCon = new DatabaseConnection();
		builder = new StringBuilder();
		con = dbCon.getConnection();
		builder.append("INSERT INTO library.book( ");
		builder.append("title, format, editor_fk, page_number, publication_date, summary, idx, sale_price, ");
		builder.append("stock, cost_price, profit_margin, isbn, cover_directory) ");
		builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		dml = builder.toString();
		bookAutorDAO = new BookAutorDaoImpl();
		bookCategoryDAO = new BookCategoryDaoImpl();
		
		try {
			con.setAutoCommit(false);
			ps = con.prepareStatement(dml);
			ps.setString(1, book.getTitle());
			ps.setString(2, book.getFormat());
			ps.setString(3, book.getEditor().getName());
			ps.setShort(4,  book.getPageNumber());
			java.sql.Date sqlDate = new java.sql.Date(book.getPublicationDate().getTime());
			ps.setDate(5, sqlDate);
			ps.setString(6, book.getSummary());
			ps.setString(7, book.getIndex());
			ps.setDouble(8, book.getSalePrice());
			ps.setInt(9, book.getStockQuantity());
			ps.setDouble(10, book.getCostPrice());
			ps.setDouble(11, book.getProfitMargin());
			ps.setString(12, book.getIsbn());
			ps.setString(13, book.getCoverDirectory());
			
			if (ps.executeUpdate() > 0) {
				ps.close();
				con.commit();
				if (bookAutorDAO.addAutorsForBook(book)) {
					if (bookCategoryDAO.addCategoriesForBook(book)){
						con.close();
						return true;
					} else {
						con.rollback();
						con.close();
						return false;
					}
				} else {
					con.rollback();
					con.close();
					return false;
				}
			} else {
				return false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean updateBook(Book book, String newIsbn) {
		DatabaseConnection dbCon;
		PreparedStatement ps;
		Connection con;
		dbCon = new DatabaseConnection();
		builder = new StringBuilder();
		builder.append("UPDATE library.book ");
		builder.append("SET book.title = ?, book.format = ?, book.editor_fk = ?, book.page_number = ?, book.publication_date = ?, book.summary = ?, ");
		builder.append("book.idx = ?, book.stock = ?, book.sale_price = ?, book.cost_price = ?, book.profit_margin =?, book.isbn = ?, book.cover_directory = ? ");
		builder.append("WHERE book.isbn = ?");
		dml = builder.toString();
		bookAutorDAO = new BookAutorDaoImpl();
		bookCategoryDAO = new BookCategoryDaoImpl();
		
		try {
			con = dbCon.getConnection();
			con.setAutoCommit(false);
			
			ps = con.prepareStatement(dml);
			ps.setString(1, book.getTitle());
			ps.setString(2, book.getFormat());
			ps.setString(3, book.getEditor().getName());
			ps.setInt(4, book.getPageNumber());
			java.sql.Date sqlDate = new java.sql.Date(book.getPublicationDate().getTime());
			ps.setDate(5, sqlDate);
			ps.setString(6, book.getSummary());
			ps.setString(7, book.getIndex());
			ps.setInt(8, book.getStockQuantity());
			ps.setDouble(9, book.getSalePrice());
			ps.setDouble(10, book.getCostPrice());
			ps.setDouble(11, book.getProfitMargin());
			ps.setString(12, newIsbn);
			ps.setString(13, book.getCoverDirectory());
			ps.setString(14, book.getIsbn());
			
			if (ps.executeUpdate() > 0) {
				ps.close();
				con.commit();
				book.setIsbn(newIsbn);
				if (bookAutorDAO.updateAutorsForBook(book)) {
					if (bookCategoryDAO.updateCategoriesForBook(book)) {
						con.close();
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}	

	@Override
	public boolean removeBook(Book book) {
		DatabaseConnection dbCon;
		PreparedStatement ps;
		Connection con;
		dbCon = new DatabaseConnection();
		builder = new StringBuilder();
		con = dbCon.getConnection();
		
		builder.append("DELETE FROM library.book ");
		builder.append(" WHERE isbn = ? ");
		dml = builder.toString();
		
		try {
			con.setAutoCommit(false);
			ps = con.prepareStatement(dml);
			ps.setString(1, book.getIsbn());
			
			if (ps.executeUpdate() > 0) {
				ps.close();
				con.commit();
				con.close();
				return true;
			} else {
				ps.close();
				con.close();
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Book fetchBook(Book bookDetail) {
		DatabaseConnection dbCon;
		PreparedStatement ps;
		Connection con;
		ResultSet rs;
		dbCon = new DatabaseConnection();
		builder = new StringBuilder();
		con = dbCon.getConnection();
		Book book = new Book();
		bookAutorDAO = new BookAutorDaoImpl();
		bookCategoryDAO = new BookCategoryDaoImpl();
		
		builder.append("SELECT isbn, title, format, editor_fk, page_number, publication_date, summary, idx, stock, sale_price, cost_price, profit_margin, cover_directory FROM library.book ");
		builder.append(" WHERE isbn = ?");
		query = builder.toString();
		
		try {
			ps = con.prepareStatement(query);
			ps.setString(1, bookDetail.getIsbn());
			rs = ps.executeQuery();
			
			while (rs.next()) {
				Editor editor = new Editor();
				book.setIsbn(rs.getString(1));
				book.setTitle(rs.getString(2));
				book.setFormat(rs.getString(3));
				editor.setName(rs.getString(4));
				book.setEditor(editor);
				book.setPageNumber(rs.getShort(5));
				java.util.Date javaDate = new java.sql.Date(rs.getDate(6).getTime());
				book.setPublicationDate(javaDate);
				book.setSummary(rs.getString(7));
				book.setIndex(rs.getString(8));
				book.setStockQuantity(rs.getInt(9));
				book.setSalePrice(rs.getDouble(10));
				book.setCostPrice(rs.getDouble(11));
				book.setProfitMargin(rs.getDouble(12));
				book.setCoverDirectory(rs.getString(13));
				book.setAutors(bookAutorDAO.fetchAutorsForBook(book.getIsbn()));
				book.setCategories(bookCategoryDAO.fetchCategoriesForBook(book.getIsbn()));
			}
			ps.close();
			rs.close();
			con.close();
			return book;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}		
		return null;
	}

	@Override
	public List<Book> fetchBooks() {
		DatabaseConnection dbCon;
		PreparedStatement ps;
		Connection con;
		ResultSet rs;
		dbCon = new DatabaseConnection();
		builder = new StringBuilder();
		con = dbCon.getConnection();
		ArrayList<Book> bookList = new ArrayList<Book>();
		BookAutorDAO bookAutorDAO = new BookAutorDaoImpl();
		BookCategoryDAO bookCategoryDAO = new BookCategoryDaoImpl();
		
		builder.append("SELECT isbn, title, format, editor_fk, page_number, publication_date, summary, idx, stock, sale_price, cost_price, profit_margin, cover_directory FROM library.book");
		query = builder.toString();
		
		try {
			ps = con.prepareStatement(query);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				Book book = new Book();
				Editor editor = new Editor();
				book.setIsbn(rs.getString(1));
				book.setTitle(rs.getString(2));
				book.setFormat(rs.getString(3));
				editor.setName(rs.getString(4));
				book.setEditor(editor);
				book.setPageNumber(rs.getShort(5));
				java.util.Date javaDate = new java.sql.Date(rs.getDate(6).getTime());
				book.setPublicationDate(javaDate);
				book.setSummary(rs.getString(7));
				book.setIndex(rs.getString(8));
				book.setStockQuantity(rs.getInt(9));
				book.setSalePrice(rs.getDouble(10));
				book.setCostPrice(rs.getDouble(11));
				book.setProfitMargin(rs.getDouble(12));
				book.setCoverDirectory(rs.getString(13));
				book.setAutors(bookAutorDAO.fetchAutorsForBook(book.getIsbn()));
				book.setCategories(bookCategoryDAO.fetchCategoriesForBook(book.getIsbn()));
				bookList.add(book);
			}
			ps.close();
			rs.close();
			con.close();
			return bookList;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return null;
	}
	

//	public List<Category> fetchCategoriesForBook(Book book) {
//		List<Category> categoryList = new ArrayList<Category>();
//		DatabaseConnection dbCon;
//		PreparedStatement ps;
//		Connection con;
//		dbCon = new DatabaseConnection();
//		builder = new StringBuilder();
//		con = dbCon.getConnection();
//		ResultSet rs;
//		
//		for (Category category : book.getCategory()) {
//			builder.append("SELECT category_id FROM library.category ");
//			builder.append(" WHERE category_name = ?");
//			query = builder.toString();
//			
//			try {
//				ps = con.prepareStatement(query);
//				ps.setString(1, category.getCategory());
//				rs = ps.executeQuery();
//				
//				while (rs.next()) {
//					Category categoryWithIds = new Category();
//					categoryWithIds.setId(rs.getInt(1));
//					categoryList.add(categoryWithIds);
//				}			
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//			return categoryList;
//		}
//		return categoryList;
//	}
	
	/*public Integer fetchAutorsForBook(Book book, Connection con) {
		Integer bookAutorId = 0;
		builder = new StringBuilder();
		builder.append("SELECT book_autor_id FROM ");
		builder.append("library.book_autor, library.book, library.autor ");
		builder.append("WHERE book.book_id = book_autor.book_fk ");
		builder.append("AND autor.autor_id = book_autor.autor_fk ");
		builder.append("")
		String query
		ps = con.prepareStatement(sql)
		return bookAutorId;
	}*/

}
