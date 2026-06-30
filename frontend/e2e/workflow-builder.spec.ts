import { test, expect } from '@playwright/test';

test.describe('JavaFlow Visual Studio E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the app home page
    await page.goto('/');
  });

  test('should load visual studio and build/execute a complete workflow pipeline', async ({ page }) => {
    // 1. Verify app title and layout elements
    await expect(page).toHaveTitle(/JavaFlow/);
    await expect(page.locator('.toolbar h1')).toHaveText('JavaFlow Visual Studio');
    await expect(page.getByRole('button', { name: 'New Workflow' })).toBeVisible();

    // Register a dynamic dialog handler to handle all save/create/delete alert, prompt and confirmation dialogs
    page.on('dialog', async dialog => {
      const msg = dialog.message();
      if (msg.includes('Enter Target Export Path')) {
        await dialog.accept('/Users/kathi.s/JF/TestPosts');
      } else if (msg.includes('Workflow saved') || 
                 msg.includes('Node template created') || 
                 msg.includes('Custom template deleted') || 
                 msg.includes('delete this custom template')) {
        await dialog.accept();
      } else {
        await dialog.dismiss();
      }
    });

    // 2. Create a new workflow
    await page.getByRole('button', { name: 'New Workflow' }).click();
    
    // 3. Add default nodes to the canvas by clicking them in the sidebar
    // Click Scheduler Node
    const schedulerNodeButton = page.locator('.node-item:has-text("SCHEDULER")');
    await expect(schedulerNodeButton).toBeVisible();
    await schedulerNodeButton.click();

    // Click Database Adapter Node
    const dbAdapterNodeButton = page.locator('.node-item:has-text("DATABASE-ADAPTER")');
    await expect(dbAdapterNodeButton).toBeVisible();
    await dbAdapterNodeButton.click();

    // Click Mapper Node
    const mapperNodeButton = page.locator('.node-item:has-text("MAPPER")');
    await expect(mapperNodeButton).toBeVisible();
    await mapperNodeButton.click();

    // Click Routing Node
    const routingNodeButton = page.locator('.node-item:has-text("ROUTING-NODE")');
    await expect(routingNodeButton).toBeVisible();
    await routingNodeButton.click();

    // Click Configuration Node
    const configNodeButton = page.locator('.node-item:has-text("CONFIGURATION-NODE")');
    await expect(configNodeButton).toBeVisible();
    await configNodeButton.click();

    // Click Service Node
    const serviceNodeButton = page.locator('.node-item:has-text("SERVICE-NODE")');
    await expect(serviceNodeButton).toBeVisible();
    await serviceNodeButton.click();

    // Click Component Node
    const componentNodeButton = page.locator('.node-item:has-text("COMPONENT-NODE")');
    await expect(componentNodeButton).toBeVisible();
    await componentNodeButton.click();

    // 4. Click a node on the canvas to configure it
    // Wait for the react flow nodes to appear on the canvas
    const flowNodeElements = page.locator('.react-flow__node');
    await expect(flowNodeElements).toHaveCount(7);

    // Let's click the first node (Scheduler) to open the configuration panel
    await flowNodeElements.nth(0).click();
    
    // Verify configuration panel is open
    const configPanel = page.locator('.node-config-panel');
    await expect(configPanel).toBeVisible();
    await expect(configPanel.locator('h3')).toContainText('Configure Node:');
    
    // Fill configuration (change cron expression text input)
    const cronInput = configPanel.locator('input[type="text"]').first();
    await expect(cronInput).toBeVisible();
    await cronInput.fill('*/10 * * * * *');
    
    // Apply configurations
    await configPanel.getByRole('button', { name: 'Apply Parameters' }).click();
    await expect(configPanel).not.toBeVisible();

    // 5. Test Creating a Custom Node Template
    await page.getByRole('button', { name: '+ Create Node Template' }).click();
    
    // Verify template modal is open
    const modal = page.locator('.template-creator-modal');
    await expect(modal).toBeVisible();
    
    // Fill template metadata
    await modal.locator('input[placeholder="e.g. Kathi Adapter"]').fill('Kathi Connector');
    await modal.locator('input[placeholder="e.g. Reads details from Kathi REST APIs"]').fill('Custom template integration with Kathi REST endpoints');
    
    // Fill config fields in template creator
    await modal.locator('input[value="param1"]').fill('kathiUrl');
    await modal.locator('input[value="Parameter 1"]').fill('Kathi API URL');
    
    // Save template
    await modal.getByRole('button', { name: 'Save & Register Template' }).click();
    await expect(modal).not.toBeVisible();

    // Verify custom template exists in the sidebar
    const customTemplateItem = page.locator('.node-palette .node-item:has-text("KATHI-CONNECTOR")').first();
    await expect(customTemplateItem).toBeVisible();
    
    // Click the custom template to add it to the canvas
    await customTemplateItem.click();
    await expect(flowNodeElements).toHaveCount(8);

    // 6. Save the workflow
    await page.getByRole('button', { name: 'Save Workflow' }).click();
 
    // Test Deleting the custom template
    const deleteTemplateBtn = customTemplateItem.locator('.delete-template-btn');
    await expect(deleteTemplateBtn).toBeVisible();
    await deleteTemplateBtn.click();
    await expect(customTemplateItem).not.toBeVisible();

    // Verify Import/Export JSON buttons are present
    await expect(page.getByRole('button', { name: 'Import JSON' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Export JSON' })).toBeVisible();

    // 7. Test Action Commands (Compile, Test, Build) and view outputs in Terminal Console
    const terminalConsole = page.locator('.console-panel');
    await expect(terminalConsole).toBeVisible();

    // Click Compile
    await page.getByRole('button', { name: 'Compile' }).click();
    // Verify compile logs appear in Console Output tab
    await expect(page.locator('.console-tab.active')).toContainText('Compilation Output');
    await expect(page.locator('.console-content')).toContainText('BUILD SUCCESS');

    // Click Run Test
    await page.getByRole('button', { name: 'Run Test' }).click();
    // Verify test logs appear
    await expect(page.locator('.console-tab.active')).toContainText('Test Logs');
    await expect(page.locator('.console-content')).toContainText('Test execution finished');

    // Click Build Docker
    await page.getByRole('button', { name: 'Build Docker' }).click();
    // Verify build logs
    await expect(page.locator('.console-tab.active')).toContainText('Docker Build Stdout');
    await expect(page.locator('.console-content')).toContainText('Successfully built image: javaflow-microservice-');
  });
});
