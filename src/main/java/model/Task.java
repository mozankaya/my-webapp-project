package model;

/**
 * Represents a To-Do task with id, title, and completion status.
 */
public class Task {

  /** The unique identifier for the task. */
  private int id;

  /** The title or description of the task. */
  private String title;

  /** The completion status of the task. */
  private boolean completed;

  /**
   * Default constructor for deserialization.
   */
  public Task() {
  }

  /**
   * Constructs a Task with specified id, title, and completed status.
   *
   * @param taskId the unique identifier for the task
   * @param taskTitle the title/description of the task
   * @param isCompleted whether the task is completed
   */
  public Task(
      final int taskId,
      final String taskTitle,
      final boolean isCompleted) {
    this.id = taskId;
    this.title = taskTitle;
    this.completed = isCompleted;
  }

  /**
   * Gets the task ID.
   *
   * @return the task ID
   */
  public int getId() {
    return id;
  }

  /**
   * Sets the task ID.
   *
   * @param taskId the task ID to set
   */
  public void setId(final int taskId) {
    this.id = taskId;
  }

  /**
   * Gets the task title.
   *
   * @return the task title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the task title.
   *
   * @param taskTitle the task title to set
   */
  public void setTitle(final String taskTitle) {
    this.title = taskTitle;
  }

  /**
   * Checks if the task is completed.
   *
   * @return true if the task is completed, false otherwise
   */
  public boolean isCompleted() {
    return completed;
  }

  /**
   * Sets the task completion status.
   *
   * @param isCompleted the completion status to set
   */
  public void setCompleted(final boolean isCompleted) {
    this.completed = isCompleted;
  }

  /**
   * Returns a string representation of the task.
   *
   * @return a string containing task details
   */
  @Override
  public String toString() {
    return "Task{"
        + "id=" + id
        + ", title='" + title + '\''
        + ", completed=" + completed
        + '}';
  }

  /**
   * Returns the hash code for this task.
   *
   * @return hash code value
   */
  @Override
  public int hashCode() {
    return java.util.Objects.hash(id, title, completed);
  }

  /**
   * Checks equality with another object.
   *
   * @param obj the object to compare with
   * @return true if objects are equal, false otherwise
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Task task = (Task) obj;
    return id == task.id && completed == task.completed
        && java.util.Objects.equals(title, task.title);
  }
}
