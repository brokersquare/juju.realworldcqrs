echo 'deleting follow folders:'
find . -name target -exec ls {} \;
find . -name target -exec rm -rf {} \;
