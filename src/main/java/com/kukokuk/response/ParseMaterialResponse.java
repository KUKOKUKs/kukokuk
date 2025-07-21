package com.kukokuk.response;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParseMaterialResponse {
  private List<String> skippedUrls;
  private List<String> enqueuedUrls;

}
