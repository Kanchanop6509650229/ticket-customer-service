#!/bin/bash
# Script to help clean up secrets that might have been committed to the repository
# IMPORTANT: This script provides guidance but does not automatically remove secrets from git history

echo "Secret Cleanup Helper"
echo "====================="
echo ""
echo "IMPORTANT: This script provides guidance on how to clean up secrets"
echo "that might have been committed to your git repository."
echo ""
echo "It does NOT automatically remove secrets from git history."
echo ""
echo "Follow these steps carefully:"
echo ""

echo "1. First, make sure you have the BFG Repo-Cleaner tool:"
echo "   Download from: https://rtyley.github.io/bfg-repo-cleaner/"
echo ""

echo "2. Create a file with patterns of secrets to remove, for example 'secrets.txt':"
echo "   Example contents:"
echo "   -----------------"
echo "   password=.*"
echo "   apikey=.*"
echo "   secret=.*"
echo "   -----------------"
echo ""

echo "3. Make a backup of your repository before proceeding!"
echo ""

echo "4. Run the BFG tool to remove secrets (replace paths as needed):"
echo "   java -jar bfg.jar --replace-text secrets.txt /path/to/your/repository"
echo ""

echo "5. After BFG has modified your repository, run:"
echo "   cd /path/to/your/repository"
echo "   git reflog expire --expire=now --all && git gc --prune=now --aggressive"
echo ""

echo "6. Force push to update the remote repository:"
echo "   git push --force"
echo ""

echo "IMPORTANT NOTES:"
echo "- This will rewrite git history, which can cause issues for other collaborators"
echo "- All collaborators will need to re-clone the repository after this operation"
echo "- Consider rotating any secrets that were exposed, as they should be considered compromised"
echo "- For more complex situations, consider consulting with a security professional"
echo ""

echo "For more information, visit: https://rtyley.github.io/bfg-repo-cleaner/"
