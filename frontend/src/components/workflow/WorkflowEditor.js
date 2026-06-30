import React, { useState, useEffect, useRef } from 'react';
import { ReactFlow, MiniMap, Controls, Background, applyNodeChanges, applyEdgeChanges } from 'reactflow';
import 'reactflow/dist/style.css';
import axios from 'axios';

const WorkflowEditor = () => {
  const [workflows, setWorkflows] = useState([]);
  const [nodeTypes, setNodeTypes] = useState([]);
  const [selectedWorkflow, setSelectedWorkflow] = useState(null);
  const [nodes, setNodes] = useState([]);
  const [edges, setEdges] = useState([]);
  const [selectedNode, setSelectedNode] = useState(null);
  const [nodeConfig, setNodeConfig] = useState({});
  const [isConfigPanelOpen, setIsConfigPanelOpen] = useState(false);
  
  // Custom Node Template Creator State
  const [isTemplateModalOpen, setIsTemplateModalOpen] = useState(false);
  const [newTemplateName, setNewTemplateName] = useState('');
  const [newTemplateDesc, setNewTemplateDesc] = useState('');
  const [newTemplateFields, setNewTemplateFields] = useState([
    { key: 'param1', label: 'Parameter 1', type: 'text', defaultValue: '', description: 'Custom text field' }
  ]);

  // Console Panel State
  const [activeConsoleTab, setActiveConsoleTab] = useState('compile');
  const [compileLogs, setCompileLogs] = useState(['Select a workflow and click Compile to begin.']);
  const [testLogs, setTestLogs] = useState(['Select a workflow and click Test to run in-memory execution.']);
  const [buildLogs, setBuildLogs] = useState(['Select a workflow and click Build to package a Docker image.']);
  const [isActionLoading, setIsActionLoading] = useState(false);

  const reactFlowRef = useRef();
  const [reactFlowInstance, setReactFlowInstance] = useState(null);

  const onDragStart = (event, nodeType) => {
    event.dataTransfer.setData('application/reactflow', JSON.stringify(nodeType));
    event.dataTransfer.effectAllowed = 'move';
  };

  const onDragOver = (event) => {
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
  };

  const onDrop = (event) => {
    event.preventDefault();

    if (!reactFlowRef.current || !selectedWorkflow) return;

    const reactFlowBounds = reactFlowRef.current.getBoundingClientRect();
    const typeDataStr = event.dataTransfer.getData('application/reactflow');
    if (!typeDataStr) return;

    const nodeType = JSON.parse(typeDataStr);

    const x = event.clientX - reactFlowBounds.left;
    const y = event.clientY - reactFlowBounds.top;

    const position = reactFlowInstance
      ? reactFlowInstance.project({ x, y })
      : { x, y };

    const defaultConfigs = {};
    if (nodeType.configSchema) {
      Object.entries(nodeType.configSchema).forEach(([key, schema]) => {
        defaultConfigs[key] = schema.defaultValue !== undefined ? schema.defaultValue : '';
      });
    }

    const newNode = {
      id: `node-${crypto.randomUUID()}`,
      type: nodeType.name,
      position,
      data: {
        label: nodeType.name.toUpperCase(),
        config: defaultConfigs
      }
    };

    setNodes((nds) => [...nds, newNode]);
  };

  useEffect(() => {
    fetchWorkflows();
    fetchNodeTypes();
  }, []);

  const fetchWorkflows = async () => {
    try {
      const response = await axios.get('/api/workflows');
      setWorkflows(response.data);
    } catch (error) {
      console.error('Error fetching workflows:', error);
    }
  };

  const fetchNodeTypes = async () => {
    try {
      const response = await axios.get('/api/node-types');
      setNodeTypes(response.data);
    } catch (error) {
      console.error('Error fetching node types:', error);
    }
  };

  const handleWorkflowSelect = async (workflowId) => {
    if (!workflowId) {
      setSelectedWorkflow(null);
      setNodes([]);
      setEdges([]);
      return;
    }
    try {
      const response = await axios.get(`/api/workflows/${workflowId}`);
      setSelectedWorkflow(response.data);
      
      const mappedNodes = (response.data.nodes || []).map(node => ({
        id: node.id,
        type: node.nodeTypeId,
        position: {
          x: node.position ? node.position.x : 100,
          y: node.position ? node.position.y : 100
        },
        data: {
          label: node.name || node.id,
          config: node.config || {}
        }
      }));
      
      setNodes(mappedNodes);
      setEdges(response.data.edges || []);
    } catch (error) {
      console.error('Error fetching workflow:', error);
    }
  };

  const handleSaveWorkflow = async () => {
    if (!selectedWorkflow) return;
    
    const defaultPath = `/Users/kathi.s/JF/${selectedWorkflow.name}`;
    const exportPath = window.prompt("Enter Target Export Path to save Java files:", defaultPath);
    if (!exportPath) {
      return;
    }
    
    try {
      const mappedNodes = nodes.map(node => ({
        id: node.id,
        nodeTypeId: node.type,
        name: node.data?.label || node.id,
        config: node.data?.config || {},
        position: {
          x: node.position ? node.position.x : 100,
          y: node.position ? node.position.y : 100
        }
      }));

      const workflowData = {
        ...selectedWorkflow,
        nodes: mappedNodes,
        edges: edges
      };
      
      await axios.put(`/api/workflows/${selectedWorkflow.id}`, workflowData);
      
      setActiveConsoleTab('compile');
      setCompileLogs(['[INFO] Exporting workflow Java classes...', `[INFO] Export Path: ${exportPath}`]);
      
      const exportResponse = await axios.post(`/api/workflows/${selectedWorkflow.id}/export?path=${encodeURIComponent(exportPath)}`);
      setCompileLogs(exportResponse.data);
      
      alert('Workflow saved and exported successfully!');
    } catch (error) {
      console.error('Error saving/exporting workflow:', error);
      alert('Error saving or exporting workflow');
    }
  };
  const handleExportWorkflowJson = () => {
    if (!selectedWorkflow) return;
    
    const mappedNodes = nodes.map(node => ({
      id: node.id,
      nodeTypeId: node.type,
      name: node.data?.label || node.id,
      config: node.data?.config || {},
      position: {
        x: node.position ? node.position.x : 100,
        y: node.position ? node.position.y : 100
      }
    }));

    const workflowData = {
      name: selectedWorkflow.name,
      description: selectedWorkflow.description,
      nodes: mappedNodes,
      edges: edges
    };
    
    const jsonString = `data:text/json;charset=utf-8,${encodeURIComponent(
      JSON.stringify(workflowData, null, 2)
    )}`;
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute('href', jsonString);
    downloadAnchor.setAttribute('download', `${selectedWorkflow.name.toLowerCase().replace(/\s+/g, '-')}-definition.json`);
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  };

  const handleImportWorkflowJson = (event) => {
    const fileReader = new FileReader();
    const file = event.target.files[0];
    if (!file) return;
    
    fileReader.onload = async (e) => {
      try {
        const importedData = JSON.parse(e.target.result);
        if (!importedData.name || !importedData.nodes) {
          alert('Invalid workflow JSON file structure.');
          return;
        }
        
        const createResponse = await axios.post('/api/workflows', {
          name: `${importedData.name} (Imported)`,
          description: importedData.description || 'Imported workflow'
        });
        
        const newWorkflow = createResponse.data;
        
        const workflowData = {
          ...newWorkflow,
          nodes: importedData.nodes,
          edges: importedData.edges || []
        };
        
        await axios.put(`/api/workflows/${newWorkflow.id}`, workflowData);
        
        await fetchWorkflows();
        handleWorkflowSelect(newWorkflow.id);
        alert('Workflow imported successfully!');
      } catch (error) {
        console.error('Error importing workflow:', error);
        alert('Error importing workflow JSON file');
      }
    };
    fileReader.readAsText(file);
    event.target.value = null;
  };

  const handleCreateWorkflow = async () => {
    const newWorkflow = {
      name: `Workflow ${workflows.length + 1}`,
      description: 'New microservice workflow pipeline',
      nodes: [],
      edges: [],
      active: true
    };
    
    try {
      const response = await axios.post('/api/workflows', newWorkflow);
      setWorkflows([...workflows, response.data]);
      setSelectedWorkflow(response.data);
      setNodes([]);
      setEdges([]);
    } catch (error) {
      console.error('Error creating workflow:', error);
      alert('Error creating workflow');
    }
  };

  const handleDeleteWorkflow = async () => {
    if (!selectedWorkflow) return;
    
    if (!window.confirm('Are you sure you want to delete this workflow?')) {
      return;
    }
    
    try {
      await axios.delete(`/api/workflows/${selectedWorkflow.id}`);
      setWorkflows(workflows.filter(w => w.id !== selectedWorkflow.id));
      setSelectedWorkflow(null);
      setNodes([]);
      setEdges([]);
      setSelectedNode(null);
      setIsConfigPanelOpen(false);
      alert('Workflow deleted successfully!');
    } catch (error) {
      console.error('Error deleting workflow:', error);
      alert('Error deleting workflow');
    }
  };

  const handleAddNode = (nodeType) => {
    if (!selectedWorkflow) {
      alert('Please select or create a workflow first.');
      return;
    }
    
    const defaultConfigs = {};
    if (nodeType.configSchema) {
      Object.entries(nodeType.configSchema).forEach(([key, schema]) => {
        defaultConfigs[key] = schema.defaultValue !== undefined ? schema.defaultValue : '';
      });
    }

    const newNode = {
      id: `node-${crypto.randomUUID()}`,
      type: nodeType.name,
      position: { x: 150 + nodes.length * 30, y: 100 + nodes.length * 30 },
      data: { 
        label: nodeType.name.toUpperCase(), 
        config: defaultConfigs
      }
    };
    setNodes((nds) => [...nds, newNode]);
  };

  const onNodesChange = (changes) => {
    setNodes((nds) => applyNodeChanges(changes, nds));
  };

  const onEdgesChange = (changes) => {
    setEdges((eds) => applyEdgeChanges(changes, eds));
  };

  const onConnect = (params) => {
    setEdges((eds) => [...eds, { id: `edge-${crypto.randomUUID()}`, ...params }]);
  };

  const handleNodeClick = (event, node) => {
    setSelectedNode(node);
    setIsConfigPanelOpen(true);
    setNodeConfig(node.data && node.data.config ? node.data.config : {});
  };

  const handleNodeConfigChange = (field, value) => {
    setNodeConfig(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleApplyNodeConfig = () => {
    if (!selectedNode) return;
    
    setNodes((nds) => {
      return nds.map(node => {
        if (node.id === selectedNode.id) {
          return {
            ...node,
            data: {
              ...node.data,
              config: nodeConfig
            }
          };
        }
        return node;
      });
    });
    
    setIsConfigPanelOpen(false);
  };

  // Compile, Test, Build Actions
  const handleCompile = async () => {
    if (!selectedWorkflow) return;
    setIsActionLoading(true);
    setActiveConsoleTab('compile');
    setCompileLogs(['Compiling Spring WebFlux microservice architecture...']);
    try {
      const response = await axios.post(`/api/workflows/${selectedWorkflow.id}/compile`);
      setCompileLogs(response.data);
    } catch (error) {
      setCompileLogs(['[ERROR] Compilation failed.', error.message]);
    } finally {
      setIsActionLoading(false);
    }
  };

  const handleTest = async () => {
    if (!selectedWorkflow) return;
    setIsActionLoading(true);
    setActiveConsoleTab('test');
    setTestLogs(['Executing in-memory integration workflow test runner...']);
    try {
      const response = await axios.post(`/api/workflows/${selectedWorkflow.id}/test`);
      setTestLogs(response.data);
    } catch (error) {
      setTestLogs(['[ERROR] Test runner encountered fatal error.', error.message]);
    } finally {
      setIsActionLoading(false);
    }
  };

  const handleBuild = async () => {
    if (!selectedWorkflow) return;
    setIsActionLoading(true);
    setActiveConsoleTab('build');
    setBuildLogs(['Initializing Docker container build sequence...']);
    try {
      const response = await axios.post(`/api/workflows/${selectedWorkflow.id}/build`);
      setBuildLogs(response.data);
    } catch (error) {
      setBuildLogs(['[ERROR] Docker image creation failed.', error.message]);
    } finally {
      setIsActionLoading(false);
    }
  };

  // Custom Node Template Builder Modal Logic
  const handleAddTemplateField = () => {
    setNewTemplateFields([
      ...newTemplateFields,
      { key: `param${newTemplateFields.length + 1}`, label: `Parameter ${newTemplateFields.length + 1}`, type: 'text', defaultValue: '', description: '' }
    ]);
  };

  const handleTemplateFieldChange = (index, key, value) => {
    const updated = [...newTemplateFields];
    updated[index] = { ...updated[index], [key]: value };
    setNewTemplateFields(updated);
  };

  const handleSaveNodeTemplate = async () => {
    if (!newTemplateName) {
      alert('Template Name is required.');
      return;
    }

    const schema = {};
    newTemplateFields.forEach(f => {
      schema[f.key] = {
        label: f.label,
        type: f.type,
        defaultValue: f.defaultValue,
        description: f.description
      };
    });

    const newType = {
      name: newTemplateName.toLowerCase().replace(/\s+/g, '-'),
      description: newTemplateDesc,
      configSchema: schema
    };

    try {
      const response = await axios.post('/api/node-types', newType);
      setNodeTypes([...nodeTypes, response.data]);
      setIsTemplateModalOpen(false);
      setNewTemplateName('');
      setNewTemplateDesc('');
      setNewTemplateFields([{ key: 'param1', label: 'Parameter 1', type: 'text', defaultValue: '', description: '' }]);
      alert('Node template created successfully!');
    } catch (error) {
      console.error('Error saving template:', error);
      alert('Error creating node template');
    }
  };

  const handleDeleteTemplate = async (templateId) => {
    if (!window.confirm('Are you sure you want to delete this custom template? This action cannot be undone.')) {
      return;
    }
    try {
      await axios.delete(`/api/node-types/${templateId}`);
      fetchNodeTypes();
      alert('Custom template deleted successfully!');
    } catch (error) {
      console.error('Error deleting template:', error);
      alert('Error deleting custom template');
    }
  };

  // Filter node types by category for the sidebar
  const getCategorizedNodeTypes = (category) => {
    return nodeTypes.filter(nt => {
      const name = nt.name.toLowerCase();
      if (category === 'trigger') {
        return name.includes('scheduler') || name.includes('consumer') || name.includes('webhook') || name.includes('routing-node');
      }
      if (category === 'adapter') {
        return name.includes('database-adapter') || name.includes('mongodb') || name.includes('http-request') || name.includes('producer') || name.includes('email') || name.includes('configuration-node') || name.includes('repository-node') || name.includes('webclient-node');
      }
      if (category === 'processing') {
        return name.includes('mapper') || name.includes('converter') || name.includes('business-logic') || name.includes('filter') || name.includes('split') || name.includes('merge') || name.includes('delay') || name.includes('service-node') || name.includes('component-node') || name.includes('handler-node') || name.includes('facade-node') || name.includes('entity-node') || name.includes('pom-xml-node');
      }
      // Custom category includes custom templates added by user
      const isDefault = ['scheduler', 'consumer', 'webhook', 'database-adapter', 'mongodb', 'http-request', 'producer', 'email', 'mapper', 'converter', 'business-logic', 'filter', 'split', 'merge', 'delay', 'routing-node', 'configuration-node', 'service-node', 'component-node', 'handler-node', 'facade-node', 'entity-node', 'pom-xml-node', 'repository-node', 'webclient-node'].includes(name);
      return category === 'custom' && !isDefault;
    });
  };

  return (
    <div className="App">
      {/* Top Header Toolbar */}
      <div className="toolbar">
        <div className="toolbar-left">
          <h1>JavaFlow Visual Studio</h1>
          <button onClick={handleCreateWorkflow}>New Workflow</button>
          <button onClick={() => document.getElementById('import-json-input').click()}>Import JSON</button>
          <input 
            id="import-json-input"
            type="file" 
            accept=".json" 
            onChange={handleImportWorkflowJson} 
            style={{ display: 'none' }} 
          />
          <select 
            value={selectedWorkflow ? selectedWorkflow.id : ''} 
            onChange={(e) => handleWorkflowSelect(e.target.value)}
          >
            <option value="">Select a workflow</option>
            {workflows.map(w => (
              <option key={w.id} value={w.id}>
                {w.name}
              </option>
            ))}
          </select>
          <button onClick={handleSaveWorkflow} disabled={!selectedWorkflow}>
            Save Workflow
          </button>
          <button onClick={handleExportWorkflowJson} disabled={!selectedWorkflow}>
            Export JSON
          </button>
          <button onClick={handleDeleteWorkflow} disabled={!selectedWorkflow}>
            Delete Workflow
          </button>
        </div>

        <div className="toolbar-right">
          <button className="btn-compile" onClick={handleCompile} disabled={!selectedWorkflow || isActionLoading}>
            ⚙️ Compile
          </button>
          <button className="btn-test" onClick={handleTest} disabled={!selectedWorkflow || isActionLoading}>
            ▶️ Run Test
          </button>
          <button className="btn-build" onClick={handleBuild} disabled={!selectedWorkflow || isActionLoading}>
            📦 Build Docker
          </button>
        </div>
      </div>

      {/* Main Layout Grid */}
      <div className="editor-layout">
        {/* Left Side Node Palette */}
        <div className="node-palette">
          <div className="palette-section" style={{ borderBottom: '2px solid var(--color-indigo)' }}>
            <button 
              className="btn-primary" 
              style={{ width: '100%', background: 'linear-gradient(135deg, var(--color-indigo), #4f46e5)' }}
              onClick={() => setIsTemplateModalOpen(true)}
            >
              + Create Node Template
            </button>
          </div>

          <div className="palette-section">
            <h4>Triggers & Scheduled Layers</h4>
            {getCategorizedNodeTypes('trigger').map(nt => (
              <div 
                key={nt.id} 
                className="node-item category-trigger" 
                onClick={() => handleAddNode(nt)}
                draggable={true}
                onDragStart={(event) => onDragStart(event, nt)}
              >
                <div className="node-item-icon">⏱️</div>
                <div>
                  <strong>{nt.name.toUpperCase()}</strong>
                  <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>{nt.description}</div>
                </div>
              </div>
            ))}
          </div>

          <div className="palette-section">
            <h4>Adapters & Connectors</h4>
            {getCategorizedNodeTypes('adapter').map(nt => (
              <div 
                key={nt.id} 
                className="node-item category-adapter" 
                onClick={() => handleAddNode(nt)}
                draggable={true}
                onDragStart={(event) => onDragStart(event, nt)}
              >
                <div className="node-item-icon">🔌</div>
                <div>
                  <strong>{nt.name.toUpperCase()}</strong>
                  <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>{nt.description}</div>
                </div>
              </div>
            ))}
          </div>

          <div className="palette-section">
            <h4>Core Processing Layers</h4>
            {getCategorizedNodeTypes('processing').map(nt => (
              <div 
                key={nt.id} 
                className="node-item" 
                onClick={() => handleAddNode(nt)}
                draggable={true}
                onDragStart={(event) => onDragStart(event, nt)}
              >
                <div className="node-item-icon">⚙️</div>
                <div>
                  <strong>{nt.name.toUpperCase()}</strong>
                  <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>{nt.description}</div>
                </div>
              </div>
            ))}
          </div>

          <div className="palette-section">
            <h4>Custom Templates</h4>
            {getCategorizedNodeTypes('custom').map(nt => (
              <div 
                key={nt.id} 
                className="node-item category-custom" 
                onClick={() => handleAddNode(nt)}
                draggable={true}
                onDragStart={(event) => onDragStart(event, nt)}
                style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%' }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: 1, minWidth: 0 }}>
                  <div className="node-item-icon">🛠️</div>
                  <div style={{ minWidth: 0 }}>
                    <strong style={{ display: 'block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {nt.name.toUpperCase()}
                    </strong>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {nt.description}
                    </div>
                  </div>
                </div>
                <button
                  className="delete-template-btn"
                  title="Delete Template"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDeleteTemplate(nt.id);
                  }}
                  style={{
                    background: 'transparent',
                    border: 'none',
                    color: 'var(--text-muted)',
                    fontSize: '1rem',
                    cursor: 'pointer',
                    padding: '4px 6px',
                    marginLeft: '4px',
                    borderRadius: '4px',
                    zIndex: 10,
                    lineHeight: 1
                  }}
                  onMouseOver={(e) => e.target.style.color = '#ef4444'}
                  onMouseOut={(e) => e.target.style.color = 'var(--text-muted)'}
                >
                  🗑️
                </button>
              </div>
            ))}
            {getCategorizedNodeTypes('custom').length === 0 && (
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textAlign: 'center', padding: '12px' }}>
                No custom templates created yet.
              </div>
            )}
          </div>
        </div>

        {/* Center Canvas Area & Bottom Console */}
        <div className="react-flow-container">
          <div 
            className="react-flow-wrapper"
            onDragOver={onDragOver}
            onDrop={onDrop}
          >
            {selectedWorkflow ? (
              <ReactFlow
                ref={reactFlowRef}
                nodes={nodes}
                edges={edges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onConnect={onConnect}
                onNodeClick={handleNodeClick}
                snapToGrid={true}
                defaultZoom={1}
                minZoom={0.5}
                maxZoom={2}
                onInit={setReactFlowInstance}
              >
                <Background color="#1e293b" gap={16} />
                <Controls />
                <MiniMap style={{ backgroundColor: 'var(--bg-card)', border: '1px solid var(--bg-border)' }} />
              </ReactFlow>
            ) : (
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: 'var(--text-secondary)' }}>
                Please create or select a workflow pipeline definition from the toolbar above.
              </div>
            )}

            {/* Parameter Config Panel */}
            {isConfigPanelOpen && selectedNode && (
              <div className="node-config-panel">
                <div className="panel-header">
                  <h3>Configure Node: {selectedNode.data?.label || selectedNode.id}</h3>
                  <button onClick={() => setIsConfigPanelOpen(false)} className="close-btn">×</button>
                </div>
                
                <NodeConfigForm 
                  nodeType={nodeTypes.find(nt => nt.name === selectedNode.type) || null}
                  currentConfig={nodeConfig}
                  onChange={handleNodeConfigChange}
                  onApply={handleApplyNodeConfig}
                />
              </div>
            )}
          </div>

          {/* Bottom Console Panel */}
          <div className="console-panel">
            <div className="console-header">
              <div className="console-tabs">
                <button 
                  className={`console-tab ${activeConsoleTab === 'compile' ? 'active' : ''}`}
                  onClick={() => setActiveConsoleTab('compile')}
                >
                  🛠️ Compilation Output
                </button>
                <button 
                  className={`console-tab ${activeConsoleTab === 'test' ? 'active' : ''}`}
                  onClick={() => setActiveConsoleTab('test')}
                >
                  🚀 Test Logs
                </button>
                <button 
                  className={`console-tab ${activeConsoleTab === 'build' ? 'active' : ''}`}
                  onClick={() => setActiveConsoleTab('build')}
                >
                  🐳 Docker Build Stdout
                </button>
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                {isActionLoading ? '⏳ Running Process...' : '⚡ Ready'}
              </div>
            </div>

            <div className="console-content">
              {activeConsoleTab === 'compile' && compileLogs.map((log, index) => (
                <div key={index} className="console-line">{log}</div>
              ))}
              {activeConsoleTab === 'test' && testLogs.map((log, index) => (
                <div key={index} className="console-line" style={{ color: log.includes('✅') || log.includes('SUCCESS') ? '#10b981' : log.includes('❌') || log.includes('ERROR') ? '#ef4444' : '#38bdf8' }}>{log}</div>
              ))}
              {activeConsoleTab === 'build' && buildLogs.map((log, index) => (
                <div key={index} className="console-line" style={{ color: '#a5b4fc' }}>{log}</div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Custom Node Creator Modal */}
      {isTemplateModalOpen && (
        <>
          <div className="modal-backdrop" onClick={() => setIsTemplateModalOpen(false)}></div>
          <div className="template-creator-modal">
            <div className="panel-header" style={{ marginBottom: '16px', paddingBottom: '12px' }}>
              <h3 style={{ margin: 0 }}>Create Custom Node Template</h3>
              <button className="close-btn" onClick={() => setIsTemplateModalOpen(false)}>×</button>
            </div>

            <div className="form-group">
              <label>Template Name</label>
              <input 
                type="text" 
                value={newTemplateName} 
                onChange={(e) => setNewTemplateName(e.target.value)} 
                placeholder="e.g. Kathi Adapter"
              />
            </div>

            <div className="form-group">
              <label>Description</label>
              <input 
                type="text" 
                value={newTemplateDesc} 
                onChange={(e) => setNewTemplateDesc(e.target.value)} 
                placeholder="e.g. Reads details from Kathi REST APIs"
              />
            </div>

            <h4 style={{ margin: '20px 0 10px 0', borderBottom: '1px solid var(--bg-border)', paddingBottom: '8px' }}>Config Schema Fields</h4>

            <div style={{ maxHeight: '200px', overflowY: 'auto', marginBottom: '16px' }}>
              {newTemplateFields.map((field, idx) => (
                <div key={idx} style={{ padding: '12px', border: '1px solid var(--bg-border)', borderRadius: '6px', marginBottom: '8px', backgroundColor: 'rgba(255,255,255,0.01)' }}>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', marginBottom: '8px' }}>
                    <div>
                      <label style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Field Key</label>
                      <input 
                        type="text" 
                        value={field.key} 
                        onChange={(e) => handleTemplateFieldChange(idx, 'key', e.target.value)} 
                        style={{ padding: '6px 10px', fontSize: '0.8rem' }}
                      />
                    </div>
                    <div>
                      <label style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Label</label>
                      <input 
                        type="text" 
                        value={field.label} 
                        onChange={(e) => handleTemplateFieldChange(idx, 'label', e.target.value)} 
                        style={{ padding: '6px 10px', fontSize: '0.8rem' }}
                      />
                    </div>
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                    <div>
                      <label style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Field Input Type</label>
                      <select 
                        value={field.type} 
                        onChange={(e) => handleTemplateFieldChange(idx, 'type', e.target.value)}
                        style={{ padding: '6px 10px', fontSize: '0.8rem', width: '100%' }}
                      >
                        <option value="text">Text Input</option>
                        <option value="textarea">Text Area</option>
                        <option value="boolean">Checkbox / Toggle</option>
                      </select>
                    </div>
                    <div>
                      <label style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Default Value</label>
                      <input 
                        type="text" 
                        value={field.defaultValue} 
                        onChange={(e) => handleTemplateFieldChange(idx, 'defaultValue', e.target.value)} 
                        style={{ padding: '6px 10px', fontSize: '0.8rem' }}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div style={{ display: 'flex', gap: '8px', marginBottom: '24px' }}>
              <button onClick={handleAddTemplateField} style={{ fontSize: '0.8rem', padding: '6px 12px' }}>
                + Add Another Field
              </button>
            </div>

            <div style={{ display: 'flex', gap: '12px' }}>
              <button 
                className="btn-primary" 
                onClick={handleSaveNodeTemplate}
                style={{ flex: 1, padding: '10px' }}
              >
                Save & Register Template
              </button>
              <button 
                className="btn-secondary" 
                onClick={() => setIsTemplateModalOpen(false)}
                style={{ flex: 1, padding: '10px' }}
              >
                Cancel
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

const NodeConfigForm = ({ nodeType, currentConfig, onChange, onApply }) => {
  const [config, setConfig] = useState(currentConfig || {});
  
  useEffect(() => {
    setConfig(currentConfig || {});
  }, [currentConfig]);
  
  const handleChange = (field, value) => {
    setConfig(prev => ({
      ...prev,
      [field]: value
    }));
    onChange(field, value);
  };
  
  const renderFields = () => {
    if (!nodeType || !nodeType.configSchema) {
      return <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>No configuration parameters for this node type</p>;
    }
    
    return Object.entries(nodeType.configSchema).map(([key, schema]) => {
      const value = config[key] !== undefined ? config[key] : (schema.defaultValue || '');
      const isSecret = schema.sensitive || false;
      
      return (
        <div key={key} className="form-group">
          <label>{schema.label || key}</label>
          {schema.type === 'boolean' ? (
            <div className="checkbox-group" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <input
                type="checkbox"
                checked={!!value}
                onChange={(e) => handleChange(key, e.target.checked)}
                style={{ width: 'auto' }}
              />
              <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Enable {schema.label || key}</span>
            </div>
          ) : schema.type === 'select' ? (
            <select
              value={value}
              onChange={(e) => handleChange(key, e.target.value)}
            >
              {schema.options?.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          ) : schema.type === 'textarea' ? (
            <textarea
              value={value}
              onChange={(e) => handleChange(key, e.target.value)}
              rows={5}
              placeholder={schema.placeholder || ''}
            />
          ) : (
            <input
              type={isSecret ? 'password' : 'text'}
              value={value}
              onChange={(e) => handleChange(key, e.target.value)}
              placeholder={schema.placeholder || ''}
            />
          )}
          {schema.description && <small className="form-hint">{schema.description}</small>}
        </div>
      );
    });
  };
  
  return (
    <div>
      {renderFields()}
      <div className="form-actions">
        <button onClick={onApply} className="btn-primary" style={{ width: '100%' }}>Apply Parameters</button>
      </div>
    </div>
  );
};

export default WorkflowEditor;
