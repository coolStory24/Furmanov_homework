package com.application.bookService.book.dto.response;

import com.application.bookService.author.dto.response.GetAuthorResponse;
import com.application.bookService.tag.dto.response.GetTagResponse;
import java.util.List;

public record GetBookResponse(
    Long id, String title, GetAuthorResponse author, List<GetTagResponse> tags) {}
