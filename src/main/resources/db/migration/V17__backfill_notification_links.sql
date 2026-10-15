UPDATE notifications n
SET link = '/tasks/' || (SELECT MAX(t.id) FROM tasks t WHERE n.title = 'New comment on task: ' || t.title)
WHERE n.link IS NULL
  AND n.title LIKE 'New comment on task: %'
  AND EXISTS (SELECT 1 FROM tasks t WHERE n.title = 'New comment on task: ' || t.title);

UPDATE notifications SET link = '/meetings'
WHERE link IS NULL AND message LIKE '% scheduled a meeting "%';

UPDATE notifications SET link = '/attendance'
WHERE link IS NULL AND (title LIKE 'Leave request%');
