package com.application.bookService;

import com.application.bookService.dto.BookResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/books-view")
public class BookViewController {
  private final BookService bookService;

  @Autowired
  public BookViewController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping()
  public String viewBooks(Model model, @RequestParam(required = false) String tag) {
    List<BookResponse.GetBook> books = bookService.getAllBooks(tag);

    model.addAttribute("books", books);
    return "books";
  }
}
