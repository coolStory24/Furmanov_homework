package com.application.bookService;

import com.application.bookService.dto.BookResponse.CreateBookResponse;
import com.application.bookService.dto.BookResponse.GetBookResponse;
import com.application.bookService.exception.BookExceptions;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookService {
  private final BookRepository bookRepository;

  @Autowired
  public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public CreateBookResponse createBook(String title, String author, List<String> tags) {
    var book = bookRepository.createBook(title, author, new HashSet<>(tags));
    return new CreateBookResponse(book.id(), book.title(), book.author(), book.getTagList());
  }

  public GetBookResponse findBookById(Integer id) throws BookExceptions.BookNotFoundException {
    var book = bookRepository.findBookById(id);
    return new GetBookResponse(book.id(), book.title(), book.author(), book.getTagList());
  }

  public List<GetBookResponse> getAllBooks(String tag) {
    var books = (tag == null ? bookRepository.getAllBooks() : bookRepository.getAllBooksByTag(tag));
    return books.stream()
        .map(
            (book ->
                new GetBookResponse(book.id(), book.title(), book.author(), book.getTagList())))
        .toList();
  }

  public void updateBook(Integer id, String title, String author, List<String> tags)
      throws BookExceptions.BookNotFoundException {
    bookRepository.updateBook(id, title, author, new HashSet<>(tags));
  }

  public void deleteBook(Integer id) {
    bookRepository.deleteBook(id);
  }
}
