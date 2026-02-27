package dao;

import model.Task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Task entity.
 * Handles all database operations for tasks using SQLite.
 */
public class TaskDAO {

  /** Database URL for SQLite connection. */
  private static final String DB_URL = "jdbc:sqlite:todo.db";

  /** Logger for TaskDAO operations. */
  private static final Logger LOGGER =
      Logger.getLogger(TaskDAO.class.getName());

  /** Parameter index for query operations. */
  private static final int PARAM_ID = 1;

  /** Parameter index for completed field. */
  private static final int PARAM_COMPLETED = 2;

  /** Parameter index for ID in update operations. */
  private static final int PARAM_ID_UPDATE = 3;

  /**
   * Initializes the database connection and creates the tasks table if needed.
   */
  public TaskDAO() {
    initializeDatabase();
  }

  /**
   * Initializes the database and creates the tasks table.
   */
  private void initializeDatabase() {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (final ClassNotFoundException e) {
      LOGGER.log(Level.SEVERE,
          "SQLite driver not found in classpath", e);
      throw new ExceptionInInitializerError(
          "SQLite driver not available");
    }

    final String sql =
        "CREATE TABLE IF NOT EXISTS tasks ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "title TEXT NOT NULL, "
            + "completed INTEGER NOT NULL)";

    try (Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement()) {
      stmt.execute(sql);
    } catch (final SQLException e) {
      LOGGER.log(Level.SEVERE,
          "Failed to initialize database", e);
      throw new ExceptionInInitializerError(
          "Database initialization failed");
    }
  }

  /**
   * Retrieves all tasks from the database.
   *
   * @return a list of all tasks
   */
  public List<Task> getAllTasks() {
    final List<Task> tasks = new ArrayList<>();
    final String sql = "SELECT * FROM tasks";

    try (Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        final Task task = new Task(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getInt("completed") == 1
        );
        tasks.add(task);
      }
    } catch (final SQLException e) {
      LOGGER.log(Level.SEVERE,
          "Failed to retrieve all tasks", e);
    }
    return tasks;
  }

  /**
   * Retrieves a task by its ID.
   *
   * @param id the task ID to search for
   * @return the Task object if found, null otherwise
   */
  public Task getTaskById(final int id) {
    final String sql = "SELECT * FROM tasks WHERE id = ?";

    try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(PARAM_ID, id);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return new Task(
              rs.getInt("id"),
              rs.getString("title"),
              rs.getInt("completed") == 1
          );
        }
      }
    } catch (final SQLException e) {
      LOGGER.log(Level.SEVERE,
          "Failed to retrieve task with id: " + id, e);
    }
    return null;
  }

  /**
   * Adds a new task to the database.
   *
   * @param task the task object to add
   * @return true if the task was successfully added, false otherwise
   * @throws RuntimeException if database operation fails
   */
  public boolean addTask(final Task task) {
    final String sql =
        "INSERT INTO tasks (title, completed) VALUES (?, ?)";

    try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(PARAM_ID, task.getTitle());
      pstmt.setInt(PARAM_COMPLETED, task.isCompleted() ? 1 : 0);
      final int updated = pstmt.executeUpdate();

      if (updated == 0) {
        LOGGER.log(Level.WARNING,
            "No rows inserted for task: " + task.getTitle());
        return false;
      }
      return true;
    } catch (final SQLException e) {
      LOGGER.log(Level.SEVERE,
          "Failed to add task", e);
      throw new RuntimeException("Database error: cannot add task", e);
    }
  }

  /**
   * Updates an existing task in the database.
   *
   * @param task the task object with updated information
   * @return true if the task was successfully updated, false otherwise
   */
  public boolean updateTask(final Task task) {
    final String sql =
        "UPDATE tasks SET title = ?, completed = ? WHERE id = ?";

    try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(PARAM_ID, task.getTitle());
      pstmt.setInt(PARAM_COMPLETED, task.isCompleted() ? 1 : 0);
      pstmt.setInt(PARAM_ID_UPDATE, task.getId());
      return pstmt.executeUpdate() > 0;
    } catch (final SQLException e) {
      LOGGER.log(Level.SEVERE,
          "Failed to update task with id: " + task.getId(), e);
      return false;
    }
  }

  /**
   * Deletes a task from the database.
   *
   * @param id the ID of the task to delete
   * @return true if the task was successfully deleted, false otherwise
   */
  public boolean deleteTask(final int id) {
    final String sql = "DELETE FROM tasks WHERE id = ?";

    try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(PARAM_ID, id);
      return pstmt.executeUpdate() > 0;
    } catch (final SQLException e) {
      LOGGER.log(Level.SEVERE,
          "Failed to delete task with id: " + id, e);
      return false;
    }
  }
}
