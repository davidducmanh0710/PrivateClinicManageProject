package com.spring.privateClinicManage.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.privateClinicManage.dto.BlogDto;
import com.spring.privateClinicManage.dto.CommentDto;
import com.spring.privateClinicManage.dto.CountDto;
import com.spring.privateClinicManage.entity.Blog;
import com.spring.privateClinicManage.entity.Comment;
import com.spring.privateClinicManage.entity.CommentBlog;
import com.spring.privateClinicManage.entity.LikeBlog;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.service.BlogService;
import com.spring.privateClinicManage.service.CommentBlogService;
import com.spring.privateClinicManage.service.CommentService;
import com.spring.privateClinicManage.service.LikeBlogService;
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
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private LikeBlogService likeBlogService;


	@GetMapping(path = "/blogs/")
	@CrossOrigin
	public ResponseEntity<Object> getAllBlogs(@RequestParam Map<String, String> params) {

		User currentUser = userService.getCurrentLoginUser();

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

			if (currentUser != null) {
				LikeBlog likeBlog = likeBlogService.findLikeBlogByUserAndBlog(currentUser, b);
				Boolean hasLiked = likeBlog == null ? false : likeBlog.getHasLiked();
				b.setHasLiked(hasLiked);
			} else {
				b.setHasLiked(false);
			}

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

	@PostMapping(path = "/blogs/create-comment-blog/")
	@CrossOrigin
	public ResponseEntity<Object> createCommentBlog(@RequestBody CommentDto commentDto) {

		Blog blog = blogService.findById(commentDto.getBlogId());
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
		comment.setUser(currentUser);
		comment.setContent(commentDto.getContent());

		commentService.saveComment(comment);

		CommentBlog commentBlog = new CommentBlog();
		commentBlog.setBlog(blog);
		commentBlog.setComment(comment);
		commentBlogService.saveCommentBlog(commentBlog);

		blog.setIsCommented(true);

		messagingTemplate.convertAndSend("/notify/recievedNewComment/" + blog.getUser().getId(),
				commentBlog);

		return new ResponseEntity<>(commentBlog, HttpStatus.CREATED);
	}

	@GetMapping(path = "/blogs/{blogId}/count-likes/")
	@CrossOrigin
	public ResponseEntity<Object> countLikeBlog(@PathVariable("blogId") Integer blogId) {

		Blog blog = blogService.findById(blogId);
		if (blog == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		Integer countLikesBlog = likeBlogService.countLikeBlogByBlog(blog);

		return new ResponseEntity<>(new CountDto(countLikesBlog), HttpStatus.OK);
	}

	@PostMapping(path = "/blogs/{blogId}/likes/")
	@CrossOrigin
	@Async
	public ResponseEntity<Object> toggleLikeBlog(@PathVariable("blogId") Integer blogId) {

		User currentUser = userService.getCurrentLoginUser();
		Blog blog = blogService.findById(blogId);

		if (blog == null || currentUser == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		LikeBlog likeBlog = likeBlogService.findLikeBlogByUserAndBlog(currentUser, blog);
		if (likeBlog != null) {

			Boolean hasLiked = likeBlog.getHasLiked();

			likeBlog.setHasLiked(!hasLiked);
			blog.setHasLiked(!hasLiked);
			likeBlog.setBlog(blog);

			likeBlogService.saveLikeBlog(likeBlog);

			return new ResponseEntity<>(likeBlog, HttpStatus.OK);
		}

		likeBlog = new LikeBlog();
		likeBlog.setBlog(blog);
		likeBlog.setUser(currentUser);
		likeBlog.setHasLiked(true);
		blog.setHasLiked(true);
		likeBlogService.saveLikeBlog(likeBlog);

		return new ResponseEntity<>(likeBlog, HttpStatus.OK);
	}


}
