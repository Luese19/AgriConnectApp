package org.scitechdev.finalPorject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatViewController {

    @GetMapping("/chat")
    public String getChatPage() {
        return "chat";
    }
}
