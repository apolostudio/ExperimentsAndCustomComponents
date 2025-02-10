self.onmessage = async (e) => {
    try {
        const { action, id, data, extension } = e.data;
        let result;

        switch (action) {
            case 'encode':
                result = await encodeFile(id, data, extension);
                break;
            case 'decode':
                result = await decodeFile(id, extension);
                break;
            case 'delete':
                result = await deleteFile(id);
                break;
            default:
                throw new Error('Invalid action');
        }

        postMessage(result);
    } catch (error) {
        postMessage({ 
            action: 'error',
            error: error.message,
            stack: error.stack
        });
    }
};

async function encodeFile(id, data, extension) {
    const root = await navigator.storage.getDirectory();
    const filename = `${id}.${extension}`;
    const fileHandle = await root.getFileHandle(filename, { create: true });
    const accessHandle = await fileHandle.createSyncAccessHandle();
    
    try {
        accessHandle.truncate(0);
        accessHandle.write(data);
        accessHandle.flush();
        return { 
            action: 'encoded',
            id,
            filename,
            size: data.byteLength
        };
    } finally {
        accessHandle.close();
    }
}

async function decodeFile(id, extension) {
    const root = await navigator.storage.getDirectory();
    const filename = `${id}.${extension}`;
    const fileHandle = await root.getFileHandle(filename);
    const accessHandle = await fileHandle.createSyncAccessHandle();
    
    try {
        const size = accessHandle.getSize();
        const buffer = new Uint8Array(size);
        accessHandle.read(buffer);
        return {
            action: 'decoded',
            id,
            data: buffer.buffer,
            extension,
            size
        };
    } finally {
        accessHandle.close();
    }
}

async function deleteFile(id) {
    const root = await navigator.storage.getDirectory();
    await root.removeEntry(id);
    return { action: 'deleted', id };
}
