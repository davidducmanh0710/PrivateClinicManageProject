package com.spring.privateClinicManage.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.privateClinicManage.dto.BlogDto;
import com.spring.privateClinicManage.entity.Blog;
import com.spring.privateClinicManage.entity.Comment;
import com.spring.privateClinicManage.entity.CommentBlog;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.service.BlogService;
import com.spring.privateClinicManage.service.CommentBlogService;
import com.spring.privateClinicManage.service.CommentService;
import com.spring.privateClinicManage.service.UserService;

@RestController
@RequestMapping("/api/anyrole")
public class ApiAnyRoleRestController {
	
	@Autowired
	private UserService userService;
	@Autowired
	private BlogService blogService;
	@Autowired
	private CommentService commentService;
	@Autowired
	private CommentBlogService commentBlogService;

	@GetMapping(path = "/blogs/")
	@CrossOrigin
	public ResponseEntity<Object> getAllBlogs(@RequestParam Map<String, String> params) {

		Integer page = Integer.parseInt(params.getOrDefault("page", "1"));
		Integer size = Integer.parseInt(params.getOrDefault("size", "5"));

		List<Blog> blogs;

		String key = params.getOrDefault("key", "");

		if (!key.isBlank()) {

			blogs = blogService.findByAnyKey(key);
		}
		else
			blogs = blogService.findAllBlogs();

		blogs.stream().forEach(b -> {
			List<CommentBlog> cb = commentBlogService.findByBlog(b);
			Boolean isCommented = cb == null || cb.size() < 1 ? false : true;
			b.setIsCommented(isCommented);
		});

		Page<Blog> allBlogsPaginated = blogService
				.allBlogsPaginated(page, size, blogs);

		return new ResponseEntity<Object>(allBlogsPaginated, HttpStatus.OK);
	}

	@PostMapping(path = "/blogs/create/")
	@CrossOrigin
	public ResponseEntity<Object> createBlog(@RequestBody BlogDto blogDto) {

		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		Blog blog = new Blog();
		blog.setTitle(blogDto.getTitle());
		blog.setContent(blogDto.getContent());
		blog.setUser(currentUser);
		blog.setCreatedDate(new Date());
		blogService.saveBlog(blog);

		return new ResponseEntity<>(blog, HttpStatus.CREATED);
	}

	@GetMapping(path = "/blogs/{blogId}/get-comment-blog/")
	@CrossOrigin
	public ResponseEntity<Object> getCommentBlogByBlog(@PathVariable("blogId") Integer blogId) {
		Blog blog = blogService.findById(blogId);
		if (blog == null)
			return new ResponseEntity<>("Bài viết này không tồn tại !", HttpStatus.NOT_FOUND);

		List<CommentBlog> cb = commentBlogService.findByBlog(blog);

		if (cb == null || cb.size() < 1)
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		return new ResponseEntity<>(cb.get(0).getComment(), HttpStatus.OK);
	}

	@PostMapping(path = "/blogs/{blogId}/create-comment-blog/")
	@CrossOrigin
	public ResponseEntity<Object> createCommentBlog(@PathVariable("blogId") Integer blogId) {

		Blog blog = blogService.findById(blogId);
		if (blog == null)
			return new ResponseEntity<>("Bài viết này không tồn tại !", HttpStatus.NOT_FOUND);

		User currentUser = userService.getCurrentLoginUser();

		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại !", HttpStatus.NOT_FOUND);

		List<CommentBlog> cb = commentBlogService.findByBlog(blog);

		if (cb.size() > 0)
			return new ResponseEntity<>("Bài viết này đã được trả lời !", HttpStatus.UNAUTHORIZED);

		Comment comment = new Comment();
		comment.setCreatedDate(new Date());
	}

}
