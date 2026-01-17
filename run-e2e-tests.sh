#!/bin/bash
# E2E Test Runner Script
# This script runs all E2E tests and generates a report

echo "========================================="
echo "Running E2E Tests for minicommerce API"
echo "========================================="
echo ""

# Run E2E tests
echo "📝 Executing E2E test suite..."
./gradlew test --tests "com.minicommerceapi.minicommerce.e2e.*" --console=plain

# Check exit code
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ All E2E tests passed!"
    echo ""
    echo "📊 View test report:"
    echo "   file://$(pwd)/build/reports/tests/test/index.html"
    echo ""
    echo "📋 Test Coverage:"
    echo "   - UserRegistrationToOrderE2ETest: User → Order workflow"
    echo "   - ProductLifecycleE2ETest: Product CRUD operations"
    echo "   - ProductReviewWorkflowE2ETest: Review management"
    echo "   - CategoryCatalogManagementE2ETest: Catalog organization"
    echo "   - MultiUserShoppingE2ETest: Multi-user scenarios"
else
    echo ""
    echo "❌ Some E2E tests failed!"
    echo "📊 View detailed report:"
    echo "   file://$(pwd)/build/reports/tests/test/index.html"
    exit 1
fi
