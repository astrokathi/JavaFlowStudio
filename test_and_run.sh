#!/bin/bash
set -e

cd /Users/kathi.s/JavaFlow

# Check if docker-compose is installed, if not install it via brew
if ! command -v docker-compose &> /dev/null
then
    echo "docker-compose could not be found, installing via brew..."
    brew install docker-compose
fi

# Fix the frontend port in docker-compose.yml (if not already fixed)
sed -i '' 's/- "3000:3000"/- "3000:80"/' docker-compose.yml

# Fix the frontend App.js to use WorkflowEditor
cat > frontend/src/App.js << 'EOF_APP'
import React from 'react';
import './App.css';
import WorkflowEditor from './components/workflow/WorkflowEditor';

function App() {
  return (
    <div className="App">
      <WorkflowEditor />
    </div>
  );
}

export default App;
EOF_APP

# Now, rebuild and start the services
docker-compose up -d --build

# Wait for the backend to be ready (max 60 seconds)
echo "Waiting for backend to be ready..."
for i in {1..12}; do
  if curl -s http://localhost:8080/actuator/health 2>/dev/null | grep -q '"status":"UP"'; then
    echo "Backend is up!"
    break
  elif curl -s http://localhost:8080/api-docs 2>/dev/null | grep -q '"openapi"'; then
    echo "API docs are available!"
    break
  else
    echo -n "."
    sleep 5
  fi
done

# If we didn't break, wait a bit more
if [ $i -eq 12 ]; then
  echo "Waiting a bit more for backend..."
  sleep 10
fi

# Now, install Playwright in the frontend and run a test
cd frontend
npx playwright install --with-deps
# Write a simple test
cat > tests/workflow.test.js << 'EOF_TEST'
const { test, expect } = require('@playwright/test');

test('should load the workflow editor and create a workflow', async ({ page }) => {
  await page.goto('http://localhost:3000');
  
  // Check if the page has loaded by looking for a known element
  // We'll look for the toolbar or the react-flow container
  await expect(page.locator('.toolbar')).toBeVisible({ timeout: 10000 });
  
  // Check if we can see the "New Workflow" button
  await expect(page.getByRole('button', { name: 'New Workflow' })).toBeVisible();
  
  // Click the "New Workflow" button
  await page.getByRole('button', { name: 'New Workflow' }).click();
  
  // Wait for the workflow editor to show the canvas
  await expect(page.locator('.react-flow-wrapper')).toBeVisible({ timeout: 5000 });
  
  // Check if we can see the dropdown for selecting a workflow
  await expect(page.locator('select')).toBeVisible();
  
  // Select the first option (which should be the newly created workflow)
  await page.selectOption('select', { index: 1 });
  
  // Wait a bit for the workflow to load
  await page.waitForTimeout(1000);
  
  // Check if we see the nodes and edges (initially empty, so we should see an empty canvas)
  // We can check for the presence of the react-flow container
  await expect(page.locator('.react-flow__panels')).toBeVisible();
  
  // Now, let's try to add a node by clicking on the canvas (we'll simulate a click at a position)
  // We'll click at position (100, 100) on the canvas
  await page.click('.react-flow__panels', { position: { x: 100, y: 100 } });
  
  // Wait for the node to appear (we expect a node to be created)
  // We'll wait for an element with the class 'react-flow__node'
  await expect(page.locator('.react-flow__node')).toBeVisible({ timeout: 5000 });
  
  // Now, let's try to save the workflow by clicking the "Save Workflow" button
  // Note: the button is disabled until we select a workflow, but we did select one above
  await expect(page.getByRole('button', { name: 'Save Workflow' })).toBeEnabled();
  await page.getByRole('button', { name: 'Save Workflow' }).click();
  
  // Wait for the alert (we'll wait for the alert to appear and then dismiss it)
  // Note: we are using alert, so we can wait for the dialog
  const [dialog] = await Promise.all([
    page.waitForEvent('dialog'),
    page.getByRole('button', { name: 'Save Workflow' }).click()
  ]);
  expect(dialog.message()).toContain('Workflow saved successfully');
  await dialog.accept();
  
  // Now, let's try to delete the workflow
  await expect(page.getByRole('button', { name: 'Delete Workflow' })).toBeEnabled();
  await page.getByRole('button', { name: 'Delete Workflow' }).click();
  
  // Wait for the confirmation dialog
  const [confirmDialog] = await Promise.all([
    page.waitForEvent('dialog'),
    page.getByRole('button', { name: 'Delete Workflow' }).click()
  ]);
  expect(confirmDialog.message()).toContain('Are you sure you want to delete this workflow?');
  await confirmDialog.accept();
  
  // Wait for the alert that the workflow was deleted
  const [deleteAlert] = await Promise.all([
    page.waitForEvent('dialog'),
    page.getByRole('button', { name: 'Delete Workflow' }).click()
  ]);
  expect(deleteAlert.message()).toContain('Workflow deleted successfully');
  await deleteAlert.accept();
});
EOF_TEST

# Run the test
npx playwright test tests/workflow.test.js --headless

# If the test passes, we'll output success, otherwise we'll capture the error
# Then, we'll stop the containers
cd ..
docker-compose down
