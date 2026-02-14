package com.keerthan.urlshortener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private UrlRepository urlRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    // Load Home Page
    @GetMapping("/")
    public String home() {
        return "index";
    }
    // Admin Dashboard
@GetMapping("/admin")
public String adminDashboard(org.springframework.ui.Model model) {

    var urls = urlRepository.findAll(
            org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC,
                    "clickCount"
            )
    );

    model.addAttribute("urls", urls);
    model.addAttribute("totalLinks", urls.size());

    int totalClicks = urls.stream()
            .mapToInt(Url::getClickCount)
            .sum();

    model.addAttribute("totalClicks", totalClicks);

    return "admin";
}


    // Shorten URL API
    @PostMapping("/shorten")
    @ResponseBody
    public String shortenUrl(@RequestParam String url) {

        // Check if already exists
        Optional<Url> existing = urlRepository.findByOriginalUrl(url);
        if (existing.isPresent()) {
            return "http://localhost:8080/" + existing.get().getShortCode();
        }

        String shortCode = generateShortCode();

        Url newUrl = new Url();
        newUrl.setOriginalUrl(url);
        newUrl.setShortCode(shortCode);

        urlRepository.save(newUrl);

        return "http://localhost:8080/" + shortCode;
    }

    // Redirect
    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {

        Optional<Url> urlOptional = urlRepository.findByShortCode(shortCode);

        if (urlOptional.isPresent()) {
            Url url = urlOptional.get();

            url.setClickCount(url.getClickCount() + 1);
            urlRepository.save(url);

            return ResponseEntity
                    .status(302)
                    .location(URI.create(url.getOriginalUrl()))
                    .build();
        }

        return ResponseEntity.notFound().build();
    }

    // Short code generator
    private String generateShortCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
