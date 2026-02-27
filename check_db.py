import sqlite3, os
print('cwd', os.getcwd())
if os.path.exists('todo.db'):
    conn = sqlite3.connect('todo.db')
    c = conn.cursor()
    c.execute("SELECT name FROM sqlite_master WHERE type='table';")
    print('tables', c.fetchall())
    try:
        for row in c.execute('SELECT * FROM tasks'):
            print('row', row)
    except Exception as e:
        print('query error', e)
    conn.close()
else:
    print('no db file found')
