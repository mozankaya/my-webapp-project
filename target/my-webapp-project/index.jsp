<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>My To Do List</title>
  <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="/my-webapp-project/css/styles.css">
  <!-- bootstrap icons for pen/trash -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
  <style>
    /* additional task styles */
    .task-list { list-style: none; padding: 0; width: 100%; }
    .task-item { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; padding: 12px; background: rgba(255,255,255,0.1); border-radius: 8px; }
    .task-item input[type="text"] { border: 1px solid rgba(255,255,255,0.3); background: rgba(255,255,255,0.1); color: white; flex: 1; font-size: 1rem; padding: 6px; border-radius: 4px; }
    .task-item input[type="checkbox"] { width: 20px; height: 20px; margin-right: 12px; cursor: pointer; }
    .task-item span { flex: 1; color: white; text-align: left; font-size: 1rem; margin-left: 8px; }
    .task-item .icons { display: flex; gap: 12px; margin-left: auto; }
    .task-item .icons i { font-size: 1.3rem; cursor: pointer; transition: color 0.2s; display: inline-block; }
    .task-item .icons .bi-pencil { color: #4ecdc4; margin-right: 8px; }
    .task-item .icons .bi-pencil:hover { color: #45b8b0; }
    .task-item .icons .bi-trash { color: #ff6b6b; }
    .task-item .icons .bi-trash:hover { color: #ff5252; }
    .task-item.completed span { text-decoration: line-through; opacity: 0.6; }
    .task-item .edit-input { display: none; }
    #add-form { margin-top: 20px; display: flex; gap: 8px; }
    #add-form input { flex: 1; padding: 10px; border-radius: 4px; border: none; font-size: 1rem; }
    #add-form input::placeholder { color: #aaa; }
  </style>
</head>
<body>
  <main class="card" id="app">
    <h2>To Do List</h2>
    <ul class="task-list" id="task-list"></ul>
    <form id="add-form">
      <input type="text" id="new-task-title" placeholder="Add a new task..." required />
      <button type="submit" class="cta-button">Add</button>
    </form>
  </main>
  <script>
    const contextPath = '/my-webapp-project';
    const apiBase = contextPath + '/api/tasks';

    async function loadTasks() {
      try {
        const res = await fetch(apiBase);
        if (!res.ok) throw new Error('Failed to load tasks');
        const tasks = await res.json();
        const list = document.getElementById('task-list');
        list.innerHTML = '';
        tasks.forEach(renderTask);
      } catch (err) {
        alert('Error loading tasks: ' + err.message);
        console.error(err);
      }
    }

    function renderTask(task) {
      const li = document.createElement('li');
      li.className = 'task-item' + (task.completed ? ' completed' : '');
      li.dataset.id = task.id;

      const checkbox = document.createElement('input');
      checkbox.type = 'checkbox';
      checkbox.checked = task.completed;
      checkbox.addEventListener('change', () => toggleCompleted(task));

      const span = document.createElement('span');
      span.textContent = task.title;

      const editInput = document.createElement('input');
      editInput.type = 'text';
      editInput.value = task.title;
      editInput.className = 'edit-input';
      editInput.addEventListener('blur', () => finishEdit(task, editInput.value));
      editInput.addEventListener('keyup', e => { if (e.key === 'Enter') editInput.blur(); });

      const icons = document.createElement('div');
      icons.className = 'icons';
      const editIcon = document.createElement('i');
      editIcon.className = 'bi bi-pencil';
      editIcon.style.cursor = 'pointer';
      editIcon.addEventListener('click', () => startEdit(span, editInput));
      const deleteIcon = document.createElement('i');
      deleteIcon.className = 'bi bi-trash';
      deleteIcon.style.cursor = 'pointer';
      deleteIcon.addEventListener('click', () => deleteTask(task));

      icons.appendChild(editIcon);
      icons.appendChild(deleteIcon);

      li.appendChild(checkbox);
      li.appendChild(span);
      li.appendChild(editInput);
      li.appendChild(icons);
      document.getElementById('task-list').appendChild(li);
    }

    function startEdit(span, input) {
      span.style.display = 'none';
      input.style.display = 'inline';
      input.focus();
    }
    async function finishEdit(task, newTitle) {
      try {
        if (newTitle && newTitle !== task.title) {
          await updateTask({ ...task, title: newTitle });
        }
        loadTasks();
      } catch (err) {
        alert('Error updating task: ' + err.message);
      }
    }

    async function toggleCompleted(task) {
      try {
        await updateTask({ ...task, completed: !task.completed });
        loadTasks();
      } catch (err) {
        alert('Error toggling task: ' + err.message);
      }
    }

    async function addTask(title) {
      try {
        const payload = { title: title, completed: false };
        const res = await fetch(apiBase, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error('Failed to add task');
        loadTasks();
      } catch (err) {
        alert('Error adding task: ' + err.message);
      }
    }

    async function updateTask(task) {
      try {
        const url = apiBase + '/' + task.id;
        const res = await fetch(url, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ title: task.title, completed: task.completed })
        });
        if (!res.ok) throw new Error('Failed to update task');
      } catch (err) {
        alert('Error updating task: ' + err.message);
      }
    }

    async function deleteTask(task) {
      try {
        const res = await fetch(apiBase + '/' + task.id, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete task');
        loadTasks();
      } catch (err) {
        alert('Error deleting task: ' + err.message);
      }
    }

    document.getElementById('add-form').addEventListener('submit', e => {
      e.preventDefault();
      const input = document.getElementById('new-task-title');
      if (input.value.trim()) {
        addTask(input.value.trim());
        input.value = '';
      }
    });

    // initial load
    loadTasks();
  </script>
</body>
</html>
