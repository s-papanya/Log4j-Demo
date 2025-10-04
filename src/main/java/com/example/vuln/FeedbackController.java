// package com.example.vuln;
// import java.util.List;
// import java.util.ArrayList;
// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;

// import javax.servlet.http.HttpServletRequest;
// import java.io.IOException;

// @Controller
// public class FeedbackController {

//     private static final Logger logger = LogManager.getLogger(FeedbackController.class);

//     @GetMapping("/")
//     public String index() {
//         return "index";
//     }

//     @PostMapping(path = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//     public String login(@RequestParam("username") String username,
//                         @RequestParam("password") String password,
//                         HttpServletRequest request,
//                         Model model) {

//         String ua = request.getHeader("User-Agent");
//         String clientIp = request.getRemoteAddr();

//         // *** ช่องโหว่: log ข้อมูลจากผู้ใช้ตรง ๆ (จะไป trigger JNDI lookup) ***
//         logger.info("Login attempt - username='{}', password='{}'", username, password);
//         logger.info("User-Agent: {}", ua);
//         logger.info("Client IP: {}", clientIp);

//         // Simple authentication logic (for demo purposes)
//         if ("admin".equals(username) && "password".equals(password)) {
//             logger.info("Successful login for user: {}", username);
//             model.addAttribute("loginSuccess", true);
//             model.addAttribute("username", username);
//         } else {
//             logger.warn("Failed login attempt for user: {}", username);
//             model.addAttribute("loginError", true);
//         }

//         return "index";
//     }

//     @PostMapping(path = "/signup", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//     public String signup(@RequestParam("username") String username,
//                          @RequestParam("password") String password,
//                          @RequestParam("email") String email,
//                          HttpServletRequest request,
//                          Model model) {

//         String ua = request.getHeader("User-Agent");
//         String clientIp = request.getRemoteAddr();

//         // *** ช่องโหว่: log ข้อมูลจากผู้ใช้ตรง ๆ ***
//         logger.info("New user registration - username='{}', email='{}', password='{}'", username, email, password);
//         logger.info("User-Agent: {}", ua);
//         logger.info("Client IP: {}", clientIp);

//         // Simple registration logic
//         logger.info("User registration successful for: {}", username);
//         model.addAttribute("signupSuccess", true);
//         model.addAttribute("username", username);

//         return "index";
//     }

//     // Keep the old feedback endpoint for backward compatibility
//     @PostMapping(path = "/feedback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//     public String feedback(@RequestParam("name") String name,
//                            @RequestParam("message") String message,
//                            HttpServletRequest request,
//                            Model model) {

//         String ua = request.getHeader("User-Agent");

//         // *** ช่องโหว่: log ข้อมูลจากผู้ใช้ตรง ๆ (จะไป trigger JNDI lookup) ***
//         logger.info("New feedback from name='{}', message='{}'", name, message);
//         logger.info("User-Agent: {}", ua);

//         model.addAttribute("ok", true);
//         model.addAttribute("name", name);
//         return "index";
//     }
//     // --- Endpoint ใหม่ที่เพิ่มเข้ามา ---
//     @GetMapping("/admin/delete")
//     @ResponseBody
//     public String deleteUser(@RequestParam String user, HttpServletRequest request) {
//         // บันทึก Log การกระทำที่อันตรายนี้
//         logger.warn("ADMIN ACTION - Attempting to delete user: '{}' from IP: {}", user, request.getRemoteAddr());

//         // จำลองการลบผู้ใช้
//         if ("admin".equalsIgnoreCase(user)) {
//             logger.error("CRITICAL: Admin user has been deleted!");
//             return "User '" + user + "' has been deleted. This is a critical security issue.";
//         } else {
//             return "User '" + user + "' deleted.";
//         }
//     }
//     // --- สถานการณ์จำลองสำหรับ OutOfMemoryError ---
//     // !!! คำเตือน: โค้ดส่วนนี้เขียนขึ้นมาให้มีช่องโหว่โดยเจตนาเพื่อการสาธิตเท่านั้น !!!
//     @GetMapping("/process-items")
//     @ResponseBody
//     public String processItems(@RequestParam(defaultValue = "10") int count) {
//         logger.info("Processing {} items.", count);
//         try {
//             // จุดที่เกิดช่องโหว่: สร้าง List ขนาดใหญ่ตาม Input ของผู้ใช้โดยไม่มีการจำกัด
//             List<byte[]> memoryHog = new ArrayList<>();
//             // วนลูปเพื่อใช้หน่วยความจำจำนวนมาก
//             // 1,048,576 bytes = 1 MB
//             for (int i = 0; i < count; i++) {
//                 memoryHog.add(new byte[1_048_576]);
//             }
//             return "Successfully processed " + count + " items. Memory used: " + count + " MB.";
//         } catch (OutOfMemoryError e) {
//             logger.error("!!! OutOfMemoryError Triggered !!! Application might be unstable.", e);
//             // ในสถานการณ์จริง แอปพลิเคชันอาจจะแครชไปก่อนที่จะตอบกลับมาได้
//             return "ERROR: Out of memory. The server is under attack.";
//         }
//     }

//     @PostMapping("/upload-slip")
//     @ResponseBody
//     public String uploadSlip(@RequestParam("orderId") String orderId,
//                              @RequestParam("slipFile") MultipartFile slipFile) {

//         if (slipFile.isEmpty()) {
//             return "Error: Please select a file to upload.";
//         }

//         try {
//             // --- จุดที่เกิดช่องโหว่ ---
//             // ระบบดึงชื่อไฟล์ดั้งเดิมที่ผู้ใช้ตั้งมา เพื่อนำไปบันทึกใน Log
//             String originalFilename = slipFile.getOriginalFilename();

//             logger.info("Processing payment for Order ID: {}", orderId);
//             // Log บรรทัดนี้คือหายนะ! Log4j จะประมวลผล 'originalFilename' ที่มี Payload
//             logger.info("Received new payment slip with filename: '{}'", originalFilename);
//             // (ในโลกจริง โค้ดส่วนนี้จะทำการบันทึกไฟล์และประมวลผลต่อไป)
//             // slipFile.transferTo(new File("/path/to/save/" + originalFilename));

//             return "Slip for Order ID " + orderId + " uploaded successfully. Filename: " + originalFilename;

//         } catch (Exception e) {
//             // ถ้าเกิด DoS สำเร็จ (StackOverflowError) แอปพลิเคชันอาจจะแครชไปเลย
//             // ถ้าใช้เวอร์ชันใหม่หน่อย จะเห็น IllegalStateException ใน Log แทน
//             logger.error("An error occurred during slip processing.", e);
//             return "An unexpected error occurred.";
//         }
//     }
// }
