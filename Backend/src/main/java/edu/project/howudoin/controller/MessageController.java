package edu.project.howudoin.controller;

import edu.project.howudoin.model.Message;
import edu.project.howudoin.model.User;
import edu.project.howudoin.security.JwtUtil;
import edu.project.howudoin.service.MessageService;
import edu.project.howudoin.service.UserService;
import edu.project.howudoin.utils.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // POST /messages/send: Send a message to a friend
    @PostMapping("/messages/send")
    public ResponseEntity<APIResponse<String>> sendMessage(@RequestHeader("Authorization") String token,
                                                           @RequestBody Message message) {
        String jwt = extractJwt(token);
        String email = jwtUtil.extractEmail(jwt);

        if (!jwtUtil.validateToken(jwt, email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new APIResponse<>(0, "ERROR", "Invalid Token"));
        }

        int id = messageService.generateMessageId();
        message.setId(id);
        String result = messageService.sendMessage(message);
        return ResponseEntity.ok(new APIResponse<>(1, "SUCCESS", result));
    }

    // GET /messages: Retrieve conversation history
    @GetMapping("/messages")
    public ResponseEntity<APIResponse<List<Message>>> getMessages(@RequestHeader("Authorization") String token) {
        String jwt = extractJwt(token);
        String email = jwtUtil.extractEmail(jwt);

        if (!jwtUtil.validateToken(jwt, email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new APIResponse<>(0, "ERROR", null));
        }

        User user = userService.getUserByEmail(email);
        List<Message> messages = messageService.getMessages(user);
        if (messages.isEmpty()) {
            return ResponseEntity.ok(new APIResponse<>(0, "ERROR", null));
        }
        return ResponseEntity.ok(new APIResponse<>(1, "SUCCESS", messages));
    }

    private String extractJwt(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            // Here you can return a ResponseEntity if desired, or
            // handle this via a global exception handler.
            throw new RuntimeException("Authorization header must start with 'Bearer '");
        }
        return token.substring(7);
    }

}
