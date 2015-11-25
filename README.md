# JIRA field correspondence

The plugin maintains 'Fix Version/s' field correspondence between parent task and sub-task in JIRA. Plugin scope:

- When sub-task is created and its 'Fix Version/s' field is empty, then sub-task's 'Fix Version/s' must be set to parent's 'Fix Version/s'. 
- When sub-task is created and its 'Fix Version/s' field is not empty, then sub-task's 'Fix Version/s' must be left intact.
- When a 'Fix Version/s' value is added to a parent task, the value must be also added to each of the parent's sub-task.
- When a 'Fix Version/s' value is removed from a parent task, the value must be also removed from each of the parent's sub-task.
