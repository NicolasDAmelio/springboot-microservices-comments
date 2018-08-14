package com.ndamelio.learning.springbootmicroservicesimages;

import com.ndamelio.learning.springbootmicroservicesimages.images.Comment;
import com.ndamelio.learning.springbootmicroservicesimages.images.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Controller
public class HomeController {

    private static final String BASE_PATH = "/images";
    private static final String FILENAME = "{filename:.+}";

    private final RestTemplate restTemplate;
    private final ImageService imageService;
//    private final CommentReaderRepository commentReaderRepository;
//    private final EmployeeRepository employeeRepository;

    @Autowired
    ReactiveMongoOperations operations;

    public HomeController(ImageService imageService, RestTemplate restTemplate) {
//    public HomeController(ImageService imageService, CommentReaderRepository commentReaderRepository) {
//    public HomeController(ImageService imageService, EmployeeRepository employeeRepository) {
        this.imageService = imageService;
        this.restTemplate = restTemplate;
//        this.commentReaderRepository = commentReaderRepository;
//        this.employeeRepository = employeeRepository;
    }

    @GetMapping(value = BASE_PATH + "/" + FILENAME + "/raw", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public Mono<ResponseEntity<?>> oneRawImage(@PathVariable String filename) {
        return imageService.findOneImage(filename)
                .map(resource -> {
                    try {
                        return ResponseEntity.ok()
                                .contentLength(resource.contentLength())
                                .body(new InputStreamResource(resource.getInputStream()));
                    } catch (IOException e) {
                        return ResponseEntity.badRequest()
                                .body("Couldn't find " + filename + " => " + e.getMessage());
                    }
                });
    }

    @PostMapping(value = BASE_PATH)
    public Mono<String> createFile(@RequestPart(name = "file") Flux<FilePart> files) {
        return imageService.createImage(files).then(Mono.just("redirect:/"));
    }

    @DeleteMapping(BASE_PATH + "/" + FILENAME)
    public Mono<String> deleteFile(@PathVariable String filename) {
        return imageService.deleteImage(filename).then(Mono.just("redirect:/"));
    }

    @GetMapping("/")
    public Mono<String> index(Model model) {
        model.addAttribute("images", imageService.findAllImages());
        model.addAttribute("comments",
                imageService.findAllImages()
                .map(image ->
                        restTemplate.exchange(
                                "http://COMMENTS/comments/{imageId}",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Comment>>() {},
                                image.getId()
                        )));
        model.addAttribute("extra", "DevTools can also detect code changes too.");
        return Mono.just("index");
    }

//    @GetMapping("/example")
//    @ResponseBody
//    public Flux<Employee> testExample() {
//        Employee e = new Employee();
//        e.setLastName("baggi");
//
//        ExampleMatcher matcher = ExampleMatcher.matching()
//                .withIgnoreCase()
//                .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.startsWith())
//                .withIncludeNullValues();
//
//        Example<Employee> example = Example.of(e, matcher);
//        Flux<Employee> singleEmployee = employeeRepository.findAll(example);
//        return singleEmployee;
//    }

//    @GetMapping("/operations")
//    @ResponseBody
//    public Mono<Employee> operations() {
//        Employee e = new Employee();
//        e.setFirstName("bilbo");
//
//        ExampleMatcher matcher = ExampleMatcher.matching()
//                .withIgnoreCase()
//                .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.startsWith())
//                .withIncludeNullValues();
//
//        Example<Employee> example = Example.of(e, matcher);
//
//        Mono<Employee> singleEmployee = operations.findOne(
//                Query.query(Criteria.where("firstName").is("Frodo")), Employee.class);
//        return singleEmployee;
//    }
}
