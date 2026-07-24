export interface ImportJobStatus {
  jobId: string
  state: 'RUNNING' | 'COMPLETED' | 'FAILED'
  processedRows: number
  importedCount: number
  skippedCount: number
  errorCount: number
  startedAt: string
  finishedAt?: string
}
