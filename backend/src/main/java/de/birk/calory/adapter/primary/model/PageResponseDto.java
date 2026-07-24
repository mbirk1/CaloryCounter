package de.birk.calory.adapter.primary.model;

import java.util.List;

/**
 * Generic paginated API response envelope. A dedicated DTO is used instead of serializing
 * Spring Data's {@code Page<T>} directly, so the API contract stays stable and independent of
 * Spring Data's own JSON representation.
 *
 * @author Marius Birk
 * @param <T> the type of the paginated content elements
 */
public class PageResponseDto<T> {

  private List<T> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean last;

  public PageResponseDto() {
  }

  /**
   * Constructor that takes all properties needed to describe a page of results.
   *
   * @param content the elements of the current page
   * @param page the current, zero-based page index
   * @param size the requested page size
   * @param totalElements the total number of elements across all pages
   * @param totalPages the total number of pages
   * @param last whether this is the last page
   */
  public PageResponseDto(
      List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {
    this.content = content;
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
    this.last = last;
  }

  public List<T> getContent() {
    return content;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public boolean isLast() {
    return last;
  }
}
