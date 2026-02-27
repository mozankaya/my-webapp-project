package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dao.TaskDAO;
import model.Task;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST servlet for managing tasks.
 * Handles GET, POST, PUT, and DELETE operations on tasks.
 */
public final class TasksServlet extends HttpServlet {

  /** Logger instance for this servlet. */
  private static final Logger LOGGER =
      Logger.getLogger(TasksServlet.class.getName());

  /** Content type for JSON responses. */
  private static final String CONTENT_TYPE = "application/json";

  /** Character encoding for responses. */
  private static final String CHARSET = "UTF-8";

  /** Data access object for task operations. */
  private TaskDAO taskDAO;

  /** Gson instance for JSON serialization/deserialization. */
  private Gson gson;

  @Override
  public void init() throws ServletException {
    super.init();
    this.taskDAO = new TaskDAO();
    this.gson = new Gson();
  }

  /**
   * Handles GET requests to retrieve all tasks or a specific task by ID.
   *
   * @param req the HTTP request
   * @param resp the HTTP response
   * @throws ServletException if servlet error occurs
   * @throws IOException if IO error occurs
   */
  @Override
  protected void doGet(
      final HttpServletRequest req,
      final HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType(CONTENT_TYPE);
    resp.setCharacterEncoding(CHARSET);

    final String pathInfo = req.getPathInfo();
    try (PrintWriter out = resp.getWriter()) {
      if (pathInfo == null || "/".equals(pathInfo)) {
        final List<Task> tasks = taskDAO.getAllTasks();
        out.print(gson.toJson(tasks));
      } else {
        try {
          final int id = Integer.parseInt(pathInfo.substring(1));
          final Task task = taskDAO.getTaskById(id);
          if (task != null) {
            out.print(gson.toJson(task));
          } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
          }
        } catch (final NumberFormatException e) {
          LOGGER.log(Level.WARNING, "Invalid task ID format: " + pathInfo, e);
          resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
      }
    }
  }

  /**
   * Handles POST requests to create a new task.
   *
   * @param req the HTTP request
   * @param resp the HTTP response
   * @throws ServletException if servlet error occurs
   * @throws IOException if IO error occurs
   */
  @Override
  protected void doPost(
      final HttpServletRequest req,
      final HttpServletResponse resp)
      throws ServletException, IOException {
    final StringBuilder body = readRequestBody(req);
    Task task;

    try {
      task = gson.fromJson(body.toString(), Task.class);
    } catch (final JsonSyntaxException e) {
      LOGGER.log(Level.WARNING, "Invalid JSON in request body", e);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      try (PrintWriter out = resp.getWriter()) {
        out.print("{\"error\":\"invalid json\"}");
      }
      return;
    }

    if (task == null || task.getTitle() == null
        || task.getTitle().isEmpty()) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      try (PrintWriter out = resp.getWriter()) {
        out.print("{\"error\":\"missing title\"}");
      }
      return;
    }

    resp.setContentType(CONTENT_TYPE);
    resp.setCharacterEncoding(CHARSET);

    try {
      final boolean success = taskDAO.addTask(task);
      if (success) {
        resp.setStatus(HttpServletResponse.SC_CREATED);
        try (PrintWriter out = resp.getWriter()) {
          out.print(gson.toJson(task));
        }
      } else {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        try (PrintWriter out = resp.getWriter()) {
          out.print("{\"error\":\"unable to add task\"}");
        }
      }
    } catch (final RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Failed to add task", e);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      try (PrintWriter out = resp.getWriter()) {
        out.print("{\"error\":\"database error\"}");
      }
    }
  }

  /**
   * Handles PUT requests to update an existing task.
   *
   * @param req the HTTP request
   * @param resp the HTTP response
   * @throws ServletException if servlet error occurs
   * @throws IOException if IO error occurs
   */
  @Override
  protected void doPut(
      final HttpServletRequest req,
      final HttpServletResponse resp)
      throws ServletException, IOException {
    final String pathInfo = req.getPathInfo();
    if (pathInfo == null || "/".equals(pathInfo)) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    try {
      final int id = Integer.parseInt(pathInfo.substring(1));
      final StringBuilder body = readRequestBody(req);
      Task task;

      try {
        task = gson.fromJson(body.toString(), Task.class);
      } catch (final JsonSyntaxException e) {
        LOGGER.log(Level.WARNING, "Invalid JSON in request body", e);
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try (PrintWriter out = resp.getWriter()) {
          out.print("{\"error\":\"invalid json\"}");
        }
        return;
      }

      if (task == null || task.getTitle() == null
          || task.getTitle().isEmpty()) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try (PrintWriter out = resp.getWriter()) {
          out.print("{\"error\":\"missing title\"}");
        }
        return;
      }

      task.setId(id);
      final boolean success = taskDAO.updateTask(task);

      resp.setContentType(CONTENT_TYPE);
      resp.setCharacterEncoding(CHARSET);

      if (success) {
        try (PrintWriter out = resp.getWriter()) {
          out.print(gson.toJson(task));
        }
      } else {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
    } catch (final NumberFormatException e) {
      LOGGER.log(Level.WARNING, "Invalid task ID format: " + pathInfo, e);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Handles DELETE requests to remove a task.
   *
   * @param req the HTTP request
   * @param resp the HTTP response
   * @throws ServletException if servlet error occurs
   * @throws IOException if IO error occurs
   */
  @Override
  protected void doDelete(
      final HttpServletRequest req,
      final HttpServletResponse resp)
      throws ServletException, IOException {
    final String pathInfo = req.getPathInfo();
    if (pathInfo == null || "/".equals(pathInfo)) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    try {
      final int id = Integer.parseInt(pathInfo.substring(1));
      final boolean success = taskDAO.deleteTask(id);
      if (success) {
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
      } else {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
    } catch (final NumberFormatException e) {
      LOGGER.log(Level.WARNING, "Invalid task ID format: " + pathInfo, e);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Reads the request body from the HTTP request.
   *
   * @param req the HTTP request
   * @return a StringBuilder containing the request body
   * @throws IOException if IO error occurs
   */
  private StringBuilder readRequestBody(final HttpServletRequest req)
      throws IOException {
    final StringBuilder body = new StringBuilder();
    try (BufferedReader reader = req.getReader()) {
      String line;
      while ((line = reader.readLine()) != null) {
        body.append(line);
      }
    }
    return body;
  }
}
