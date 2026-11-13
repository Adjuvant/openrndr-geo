<!-- op:issue-convention v1 start -->
## Filing issues

Follow this whenever you file a GitHub issue, in this repo or any other.

1. Start the issue body with these two lines, exactly:

   Found in: <name of the repo you are working in>
   Blocking: yes | no

   "Blocking: yes" means the problem stops your current task in the Found in repo.

2. If you file the issue in a repo other than the one you are working in, add the label `dogfood`.

3. Describe only what you observed, where, and how to reproduce it. Do not add goals, priorities or plans from other projects.

Example, filed in pi-tools while working in bpy-tool:

   Found in: bpy-tool
   Blocking: no

   dev-chain retried the wrong tool after a step timeout...
<!-- op:issue-convention v1 end -->
