package com.dndcharactercreator.pdfimport.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enums")
public class EnumsController {

  private final com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();

  @GetMapping("/alignments")
  public java.util.List<String> getAlignments() throws java.io.IOException {
    try (var in = getClass().getClassLoader().getResourceAsStream("data/alignments.json")) {
      if (in == null) throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.NOT_FOUND, "alignments.json not found");
      return om.readValue(in, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {});
    }
  }
}