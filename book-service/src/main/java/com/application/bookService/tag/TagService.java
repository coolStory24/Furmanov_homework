package com.application.bookService.tag;

import com.application.bookService.tag.dto.response.CreateTagResponse;
import com.application.bookService.tag.dto.response.GetTagResponse;
import com.application.bookService.tag.exceptions.TagNotFoundException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagService {
  private final TagRepository tagRepository;

  @Autowired
  public TagService(TagRepository tagRepository) {
    this.tagRepository = tagRepository;
  }

  @Transactional
  public CreateTagResponse createTag(String name) {
    var createdTag = this.tagRepository.save(new Tag(name));

    return new CreateTagResponse(createdTag.getId().toString(), createdTag.getName());
  }

  @Transactional
  public GetTagResponse getTagById(UUID id) throws TagNotFoundException {
    Tag tag = this.tagRepository.findById(id).orElse(null);

    if (tag == null) throw new TagNotFoundException(id.toString());
    return new GetTagResponse(tag.getId().toString(), tag.getName());
  }

  @Transactional
  public void updateTag(UUID id, String name) throws TagNotFoundException {
    Tag tag = this.tagRepository.findById(id).orElse(null);

    if (tag == null) throw new TagNotFoundException(id.toString());

    tag.setName(name);

    tagRepository.save(tag);
  }

  @Transactional
  public void deleteTag(UUID id) {
    tagRepository.deleteById(id);
  }
}
