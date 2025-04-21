#!/bin/bash
# Script to check for potential secrets in the codebase
# This is a simple check and not a replacement for proper secret scanning tools

echo "Checking for potential secrets in the codebase..."
echo "================================================="
echo ""

# Define patterns to search for
PATTERNS=(
  "password"
  "passwd"
  "secret"
  "api[-_]?key"
  "access[-_]?key"
  "auth[-_]?token"
  "credentials"
  "private[-_]?key"
  "BEGIN (RSA|DSA|EC|OPENSSH) PRIVATE KEY"
  "jdbc:.*:.*:.*"
)

# Files to exclude from scanning
EXCLUDE_DIRS=(
  ".git"
  "target"
  "node_modules"
  ".idea"
  ".vscode"
)

# Build exclude pattern
EXCLUDE_PATTERN=""
for dir in "${EXCLUDE_DIRS[@]}"; do
  EXCLUDE_PATTERN="$EXCLUDE_PATTERN --exclude-dir=$dir"
done

# Check each pattern
for pattern in "${PATTERNS[@]}"; do
  echo "Checking for pattern: $pattern"
  echo "-----------------------------"
  
  # Use grep to find potential matches
  RESULTS=$(grep -r -i -n $EXCLUDE_PATTERN "$pattern" --include="*.java" --include="*.properties" --include="*.yml" --include="*.yaml" --include="*.xml" --include="*.json" --include="*.md" --include="*.sh" .)
  
  if [ -n "$RESULTS" ]; then
    echo "Potential secrets found:"
    echo "$RESULTS"
    echo ""
  else
    echo "No matches found."
    echo ""
  fi
done

echo "================================================="
echo "Scan complete. Review any findings carefully."
echo "Remember that this is a basic check and may have false positives."
echo "Consider using a dedicated secret scanning tool for more thorough analysis."
