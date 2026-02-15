package com.keerthan.urlshortener;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @Autowired
    private UrlRepository urlRepository;

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    // Load Home Page
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Admin Dashboard
    @GetMapping("/admin")
    public String adminDashboard(Model model) {

        var urls = urlRepository.findAll(
                Sort.by(Sort.Direction.DESC, "clickCount")
        );

        model.addAttribute("urls", urls);
        model.addAttribute("totalLinks", urls.size());

        int totalClicks = urls.stream()
                .mapToInt(Url::getClickCount)
                .sum();

        model.addAttribute("totalClicks", totalClicks);

        return "admin";
    }

    @PostMapping("/shorten")
@ResponseBody
public String shortenUrl(@RequestParam String url,
                         HttpServletRequest request) {

    Optional<Url> existing = urlRepository.findByOriginalUrl(url);

    String baseUrl = request.getScheme() + "://" +
            request.getServerName() +
            ((request.getServerPort() == 80 || request.getServerPort() == 443)
                    ? ""
                    : ":" + request.getServerPort());

    if (existing.isPresent()) {
        return baseUrl + "/" + existing.get().getShortCode();
    }

    String shortCode = generateShortCode();

    Url newUrl = new Url();
    newUrl.setOriginalUrl(url);
    newUrl.setShortCode(shortCode);
    newUrl.setClickCount(0);

    urlRepository.save(newUrl);

    return baseUrl + "/" + shortCode;
}


    // Redirect
    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {

        Optional<Url> urlOptional =
                urlRepository.findByShortCode(shortCode);

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
            sb.append(
                CHARACTERS.charAt(
                    random.nextInt(CHARACTERS.length())
                )
            );
        }

        return sb.toString();
    }
}
