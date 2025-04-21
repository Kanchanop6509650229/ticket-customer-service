#!/bin/bash
# Script to generate secure secrets for the application
# This script helps developers create secure random values for configuration

# Create scripts directory if it doesn't exist
mkdir -p $(dirname $0)

echo "Generating secure secrets for the application..."
echo "================================================"
echo ""

# Generate a secure JWT secret (64 characters)
JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
echo "JWT Secret (add to application.properties as app.jwt.secret):"
echo "$JWT_SECRET"
echo ""

# Generate a secure API key for services (48 characters)
API_KEY=$(openssl rand -base64 36 | tr -d '\n')
echo "API Key (use for event.service.api-key or other service keys):"
echo "$API_KEY"
echo ""

# Generate a secure random password (16 characters)
RANDOM_PASSWORD=$(openssl rand -base64 12 | tr -d '\n')
echo "Random Password (can be used for database or other services):"
echo "$RANDOM_PASSWORD"
echo ""

echo "IMPORTANT: Keep these values secure and never commit them to version control!"
echo "Add them to your local configuration files that are excluded by .gitignore"
