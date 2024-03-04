package com.application.bookService.book;

import com.application.bookService.book.dto.response.GetBookResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/books-view")
public class BookViewController {
  private final BookService bookService;

  @Autowired
  public BookViewController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping("/list")
  public String viewBooks(Model model) {
    List<GetBookResponse> books = bookService.getAllBooks();

    model.addAttribute("books", books);
    return "books";
  }
}
