import { test, expect } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

test('take screenshots of all nodes in Posts workflow', async ({ page }) => {
  // Ensure docs/images directory exists
  const imgDir = path.join(__dirname, '../../docs/images');
  if (!fs.existsSync(imgDir)) {
    fs.mkdirSync(imgDir, { recursive: true });
  }

  await page.goto('/');
  await page.waitForLoadState('networkidle');

  // Select the Posts workflow from dropdown
  const select = page.locator('select');
  await page.waitForFunction(() => {
    const options = Array.from(document.querySelectorAll('select option'));
    return options.some(opt => opt.textContent && opt.textContent.trim() === 'Posts');
  });
  await select.selectOption({ label: 'Posts' });
  await page.waitForTimeout(1000); // Wait for canvas render

  const nodes = [
    { name: 'pom.xml', filename: 'pom_xml_node.png' },
    { name: 'GetPostsById Route', filename: 'route_node.png' },
    { name: 'PostHandler', filename: 'handler_node.png' },
    { name: 'PostFacade', filename: 'facade_node.png' },
    { name: 'PostService', filename: 'service_node.png' },
    { name: 'PostWebClient', filename: 'webclient_node.png' },
    { name: 'PostRepository', filename: 'repository_node.png' },
    { name: 'Post', filename: 'entity_node.png', exact: true },
    { name: 'Admin DB Adapter', filename: 'db_adapter_node.png' }
  ];

  for (const node of nodes) {
    let nodeElement;
    if (node.exact) {
      nodeElement = page.locator('.react-flow__node').filter({ hasText: new RegExp(`^${node.name}$`) });
    } else {
      nodeElement = page.locator('.react-flow__node').filter({ hasText: new RegExp(`^${node.name}$`) });
      if (await nodeElement.count() === 0) {
        nodeElement = page.locator('.react-flow__node').filter({ hasText: node.name }).first();
      }
    }
    
    await expect(nodeElement).toBeVisible();
    await nodeElement.click({ force: true });
    await page.waitForTimeout(300); // Wait for config panel animation

    // Take screen screenshot
    await page.screenshot({ path: path.join(imgDir, node.filename) });
  }
});
