'use client';

import { useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { motion, AnimatePresence } from 'framer-motion';
import {
    X, Globe, Activity, Zap, CheckCircle,
    AlertCircle, FileText, ArrowUpRight,
    Pause, Play, Square
} from 'lucide-react';
import { useStopCrawl, usePauseCrawl, useResumeCrawl } from '@/hooks/useProjects';
import {
    ResponsiveContainer, AreaChart, Area, Tooltip
} from 'recharts';
import { cn } from '@/lib/utils';
import type { ProjectStatus, CrawlStatus } from '@/types';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';

interface CrawlUpdate {
    projectId: number;
    projectStatus: ProjectStatus;
    crawlStatus: CrawlStatus;
    pagesFound: number;
    pagesProcessed: number;
    chunksCreated: number;
    currentUrl: string;
    errorMessage: string;
}

interface GraphData {
    time: string;
    pages: number;
    speed: number;
}

interface CrawlOverlayProps {
    projectId: number;
    projectUrl: string;
    projectName: string;
    isOpen: boolean;
    onClose: () => void;
}

export function CrawlOverlay({ projectId, projectUrl, projectName, isOpen, onClose }: CrawlOverlayProps) {
    const [update, setUpdate] = useState<CrawlUpdate | null>(null);
    const [history, setHistory] = useState<string[]>([]);
    const [graphData, setGraphData] = useState<GraphData[]>([]);
    const [connected, setConnected] = useState(false);
    const [isStopConfirmOpen, setIsStopConfirmOpen] = useState(false);
    const lastPagesRef = useRef(0);

    const stopMutation = useStopCrawl();
    const pauseMutation = usePauseCrawl();
    const resumeMutation = useResumeCrawl();

    useEffect(() => {
        if (!isOpen || !projectId) return;

        const rawBaseUrl = process.env.NEXT_PUBLIC_SOCKET_URL || 'http://localhost:8081';
        const baseUrl = rawBaseUrl.replace(/^ws:/, 'http:').replace(/^wss:/, 'https:');
        const socketUrl = `${baseUrl}/ws`;

        console.log('STOMP Starting connection to', socketUrl, 'for project', projectId);

        const stompClient = new Client({
            webSocketFactory: () => new SockJS(socketUrl),
            debug: (str) => console.log('STOMP DEBUG:', str),
            reconnectDelay: 2000,
            heartbeatIncoming: 0,
            heartbeatOutgoing: 0,
            onConnect: (frame) => {
                console.log('STOMP Connected! Subscribing to /topic/project/' + projectId);
                setConnected(true);

                const destination = `/topic/project/${projectId}`;
                stompClient.subscribe(destination, (message) => {
                    console.log('STOMP MESSAGE RECEIVED at', destination, ':', message.body);
                    try {
                        const data: CrawlUpdate = JSON.parse(message.body);
                        setUpdate(data);

                        if (data.currentUrl) {
                            setHistory(prev => {
                                if (prev[0] === data.currentUrl) return prev;
                                return [data.currentUrl, ...prev].slice(0, 50);
                            });
                        }
                    } catch (e) {
                        console.error('STOMP Parse Error:', e);
                    }
                }, { id: `project-sub-${projectId}` });
            },
            onDisconnect: () => {
                console.log('STOMP Disconnected');
                setConnected(false);
            },
            onStompError: (frame) => {
                console.error('STOMP Protocol Error:', frame.headers['message']);
            }
        });

        stompClient.activate();

        const interval = setInterval(() => {
            setGraphData(prev => {
                const now = new Date();
                const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });

                const currentPages = lastPagesRef.current;
                const prevPages = prev.length > 0 ? prev[prev.length - 1].pages : 0;
                const speed = Math.max(0, currentPages - prevPages);

                const newData = [...prev, { time: timeStr, pages: currentPages, speed }];
                return newData.slice(-30);
            });
        }, 1500);

        return () => {
            stompClient.deactivate();
            clearInterval(interval);
        };
    }, [isOpen, projectId]);

    useEffect(() => {
        if (update) {
            lastPagesRef.current = update.pagesFound;
        }
    }, [update]);

    if (!isOpen) return null;

    const progress = update ? Math.min((update.pagesFound / 50) * 100, 100) : 0;
    const isError = update?.crawlStatus === 'FAILED' || !!update?.errorMessage;
    const isComplete = update?.crawlStatus === 'COMPLETED';

    return (
        <AnimatePresence>
            <div className="fixed inset-0 z-50 flex items-center justify-center p-6 sm:p-12">
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    onClick={onClose}
                    className="absolute inset-0 bg-black/80 backdrop-blur-md"
                />

                <motion.div
                    initial={{ opacity: 0, scale: 0.9, y: 20 }}
                    animate={{ opacity: 1, scale: 1, y: 0 }}
                    exit={{ opacity: 0, scale: 0.9, y: 20 }}
                    className="relative w-full max-w-5xl h-[85vh] top-5 bg-surface-200 border border-white/[0.1] rounded-3xl shadow-2xl flex flex-col overflow-hidden"
                >
                    <div className="px-8 py-5 flex items-center justify-between border-b border-white/[0.08] bg-white/[0.02]">
                        <div className="flex items-center gap-5">
                            <div className={cn(
                                "w-14 h-14 rounded-2xl flex items-center justify-center shadow-2xl relative group",
                                isError ? "bg-red-500/10 text-red-400" : "bg-brand-500/10 text-brand-400"
                            )}>
                                <div className="absolute inset-0 bg-brand-500/10 blur-xl opacity-50 group-hover:opacity-100 transition-opacity rounded-full ring-4 ring-brand-500/10" />
                                {isError ? <AlertCircle className="w-7 h-7 relative z-10" /> : <Activity className="w-7 h-7 relative z-20 animate-pulse" />}
                            </div>
                            <div className="space-y-0.5">
                                <div className="flex items-center gap-3">
                                    <h2 className="text-2xl font-bold text-white tracking-tight leading-none">
                                        Intelligence Scan: <span className="text-brand-400 font-extrabold">{projectName}</span>
                                    </h2>
                                    <span className={cn(
                                        "px-2 py-0.5 rounded-md text-[9px] font-bold uppercase tracking-wider",
                                        connected ? "bg-emerald-500/15 text-emerald-400 border border-emerald-500/20" : "bg-brand-500/15 text-brand-400 border border-brand-500/20"
                                    )}>
                                        {connected ? 'Live Sync' : 'Connecting...'}
                                    </span>
                                </div>
                                <p className="text-gray-400 text-xs flex items-center gap-1.5 opacity-80">
                                    <Globe className="w-3.5 h-3.5" /> {projectUrl}
                                </p>
                            </div>
                        </div>
                        <div className="flex items-center gap-4">
                            <div className="text-right hidden sm:block">
                                <p className="text-[10px] font-bold text-gray-500 uppercase tracking-widest leading-none mb-1">Current State</p>
                                <p className={cn(
                                    "text-xs font-mono font-bold uppercase",
                                    isError ? "text-red-400" : isComplete ? "text-emerald-400" : "text-brand-400"
                                )}>
                                    {update?.crawlStatus || (connected ? 'Awaiting Data...' : 'Initializing...')}
                                </p>
                            </div>
                            <button
                                onClick={onClose}
                                className="p-2.5 rounded-xl hover:bg-white/10 transition-all text-gray-400 hover:text-white border border-transparent hover:border-white/10"
                            >
                                <X className="w-6 h-6" />
                            </button>
                        </div>
                    </div>

                    <div className="flex-1 overflow-hidden p-6 sm:p-8 flex gap-8">
                        <div className="flex-1 flex flex-col gap-6 overflow-y-auto pr-2 custom-scrollbar">
                            <div className="grid grid-cols-3 gap-6">
                                <div className="bg-white/[0.03] rounded-3xl p-5 border border-white/[0.06] flex flex-col items-center justify-center text-center shadow-inner relative overflow-hidden group">
                                    <div className="absolute top-0 right-0 p-3 opacity-20 group-hover:opacity-40 transition-opacity">
                                        <FileText className="w-8 h-8 text-white" />
                                    </div>
                                    <p className="text-4xl font-extrabold text-white mb-2 tracking-tighter drop-shadow-sm">{update?.pagesFound || 0}</p>
                                    <p className="text-[10px] text-gray-500 font-bold uppercase tracking-widest bg-white/5 px-2.5 py-1 rounded-full">
                                        URLs FOUND
                                    </p>
                                </div>
                                <div className="bg-white/[0.03] rounded-3xl p-5 border border-white/[0.06] flex flex-col items-center justify-center text-center shadow-inner relative overflow-hidden group">
                                    <div className="absolute top-0 right-0 p-3 opacity-20 group-hover:opacity-40 transition-opacity">
                                        <Zap className="w-8 h-8 text-brand-400" />
                                    </div>
                                    <p className="text-4xl font-extrabold text-white mb-2 tracking-tighter drop-shadow-sm">{update?.pagesProcessed || 0}</p>
                                    <p className="text-[10px] text-gray-500 font-bold uppercase tracking-widest bg-white/5 px-2.5 py-1 rounded-full">
                                        PROCESSED
                                    </p>
                                </div>
                                <div className="bg-white/[0.03] rounded-3xl p-5 border border-white/[0.06] flex flex-col items-center justify-center text-center shadow-inner relative overflow-hidden group">
                                    <div className="absolute top-0 right-0 p-3 opacity-20 group-hover:opacity-40 transition-opacity">
                                        <ArrowUpRight className="w-8 h-8 text-emerald-400" />
                                    </div>
                                    <p className="text-4xl font-extrabold text-white mb-2 tracking-tighter drop-shadow-sm">
                                        {graphData.length > 0 ? graphData[graphData.length - 1].speed : 0}
                                    </p>
                                    <p className="text-[10px] text-gray-500 font-bold uppercase tracking-widest bg-white/5 px-2.5 py-1 rounded-full">
                                        PAGES/SEC
                                    </p>
                                </div>
                            </div>

                            <div className="space-y-3">
                                <div className="flex justify-between items-center px-1">
                                    <span className="text-[10px] font-bold text-gray-400 uppercase tracking-[0.2em]">Live Intelligence Feed Progress</span>
                                    <span className="text-xs font-mono font-bold text-brand-400 bg-brand-500/10 px-2 py-0.5 rounded border border-brand-500/20">
                                        {Math.round(progress)}% Completion
                                    </span>
                                </div>
                                <div className="relative h-3 bg-white/5 rounded-full overflow-hidden border border-white/[0.05] p-[2px]">
                                    <motion.div
                                        initial={{ width: 0 }}
                                        animate={{ width: `${progress}%` }}
                                        transition={{ type: 'spring', damping: 25, stiffness: 60 }}
                                        className="h-full bg-gradient-brand shadow-[0_0_15px_rgba(102,126,234,0.3)] rounded-full"
                                    />
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 flex-1 min-h-[220px]">
                                <div className="bg-surface-100/50 border border-white/[0.05] rounded-[2rem] p-6 flex flex-col shadow-2xl backdrop-blur-sm">
                                    <h4 className="text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-6 flex items-center gap-2 px-1">
                                        <Zap className="w-3.5 h-3.5 text-brand-400" /> Instant Speed Metric
                                    </h4>
                                    <div className="flex-1 w-full -ml-4">
                                        <ResponsiveContainer width="100%" height="100%">
                                            <AreaChart data={graphData}>
                                                <defs>
                                                    <linearGradient id="colorSpeed" x1="0" y1="0" x2="0" y2="1">
                                                        <stop offset="5%" stopColor="#667eea" stopOpacity={0.4} />
                                                        <stop offset="95%" stopColor="#667eea" stopOpacity={0} />
                                                    </linearGradient>
                                                </defs>
                                                <Area
                                                    type="monotone"
                                                    dataKey="speed"
                                                    stroke="#667eea"
                                                    fillOpacity={1}
                                                    fill="url(#colorSpeed)"
                                                    strokeWidth={3}
                                                    animationDuration={600}
                                                />
                                            </AreaChart>
                                        </ResponsiveContainer>
                                    </div>
                                </div>

                                <div className="bg-surface-100/50 border border-white/[0.05] rounded-[2rem] p-6 flex flex-col shadow-2xl backdrop-blur-sm">
                                    <h4 className="text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-6 flex items-center gap-2 px-1">
                                        <ArrowUpRight className="w-3.5 h-3.5 text-emerald-400" /> Discovery Projection
                                    </h4>
                                    <div className="flex-1 w-full -ml-4">
                                        <ResponsiveContainer width="100%" height="100%">
                                            <AreaChart data={graphData}>
                                                <defs>
                                                    <linearGradient id="colorProgress" x1="0" y1="0" x2="0" y2="1">
                                                        <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                                                        <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                                                    </linearGradient>
                                                </defs>
                                                <Area
                                                    type="monotone"
                                                    dataKey="pages"
                                                    stroke="#10b981"
                                                    fillOpacity={1}
                                                    fill="url(#colorProgress)"
                                                    strokeWidth={3}
                                                    animationDuration={600}
                                                />
                                            </AreaChart>
                                        </ResponsiveContainer>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="w-[340px] flex flex-col bg-black/30 border border-white/[0.06] rounded-[2.5rem] p-6 shadow-2xl relative overflow-hidden backdrop-blur-md">
                            <div className="absolute inset-0 bg-gradient-to-b from-brand-500/[0.02] to-transparent pointer-events-none" />
                            <div className="flex items-center justify-between mb-5 px-1 relative z-10">
                                <h4 className="text-[11px] font-extrabold text-gray-400 uppercase tracking-[0.15em]">
                                    Discovery Feed
                                </h4>
                                <div className="flex items-center gap-2 px-2 py-0.5 bg-brand-500/10 rounded-full border border-brand-500/20">
                                    <div className="w-1.5 h-1.5 rounded-full bg-brand-500 animate-pulse shadow-[0_0_8px_#667eea]" />
                                    <span className="text-[9px] text-brand-400 font-bold uppercase tracking-tight">Active</span>
                                </div>
                            </div>

                            <div className="flex-1 space-y-1.5 overflow-y-auto custom-scrollbar-compact pr-2 font-mono text-[10px] relative z-10">
                                {history.map((url, i) => (
                                    <motion.div
                                        key={`${url}-${i}`}
                                        initial={{ x: 15, opacity: 0 }}
                                        animate={{ x: 0, opacity: 1 }}
                                        className={cn(
                                            "p-2.5 rounded-2xl border transition-all duration-300",
                                            i === 0
                                                ? "bg-brand-500/15 border-brand-500/30 text-white shadow-lg ring-1 ring-brand-500/20"
                                                : "text-gray-500 hover:text-gray-300 bg-white/[0.02] border-white/5 hover:border-white/10 hover:bg-white/[0.04]"
                                        )}
                                    >
                                        <div className="flex items-center gap-3">
                                            <span className="text-[9px] font-bold text-gray-600 bg-black/20 w-8 py-0.5 rounded-lg text-center flex-shrink-0">
                                                {history.length - i}
                                            </span>
                                            <span className="truncate flex-1 font-medium">{url}</span>
                                            {i === 0 && (
                                                <div className="relative">
                                                    <CheckCircle className="w-3.5 h-3.5 text-brand-400 shrink-0" />
                                                    <div className="absolute inset-0 bg-brand-400 blur-md opacity-40 animate-pulse" />
                                                </div>
                                            )}
                                        </div>
                                    </motion.div>
                                ))}
                            </div>
                        </div>
                    </div>

                    <div className="p-8 border-t border-white/[0.06] bg-black/20 backdrop-blur-xl flex items-center justify-center gap-4">
                        {!isComplete && !isError && (
                            <>
                                {update?.crawlStatus === 'PAUSED' ? (
                                    <button
                                        onClick={() => resumeMutation.mutate(projectId)}
                                        disabled={resumeMutation.isPending}
                                        className="flex items-center gap-2 px-8 py-3 rounded-full bg-emerald-500 text-white font-bold 
                                                   shadow-lg shadow-emerald-500/20 hover:bg-emerald-600 transition-all active:scale-95 disabled:opacity-50"
                                    >
                                        <Play className="w-4 h-4 fill-current" /> Resume Scan
                                    </button>
                                ) : (
                                    <button
                                        onClick={() => pauseMutation.mutate(projectId)}
                                        disabled={pauseMutation.isPending}
                                        className="flex items-center gap-2 px-8 py-3 rounded-full bg-amber-500 text-white font-bold 
                                                   shadow-lg shadow-amber-500/20 hover:bg-amber-600 transition-all active:scale-95 disabled:opacity-50"
                                    >
                                        <Pause className="w-4 h-4 fill-current" /> Pause Scan
                                    </button>
                                )}

                                <button
                                    onClick={() => setIsStopConfirmOpen(true)}
                                    disabled={stopMutation.isPending}
                                    className="flex items-center gap-2 px-8 py-3 rounded-full bg-red-500 text-white font-bold 
                                               shadow-lg shadow-red-500/20 hover:bg-red-600 transition-all active:scale-95 disabled:opacity-50"
                                >
                                    <Square className="w-4 h-4 fill-current" /> Stop
                                </button>

                                <div className="w-px h-8 bg-white/10 mx-2" />
                            </>
                        )}

                        {isComplete || isError ? (
                            <button
                                onClick={onClose}
                                className="px-12 py-3 rounded-full bg-brand-500 text-white font-bold 
                                           shadow-lg shadow-brand-500/30 hover:bg-brand-600 transition-all active:scale-95"
                            >
                                Close Window
                            </button>
                        ) : (
                            <button
                                onClick={onClose}
                                className="px-12 py-3 rounded-full bg-white text-black font-bold 
                                           hover:bg-gray-200 transition-all active:scale-95"
                            >
                                {update?.crawlStatus === 'CANCELLED' ? 'Close' : 'Run in Background'}
                            </button>
                        )}
                    </div>
                </motion.div>

                <ConfirmDialog
                    isOpen={isStopConfirmOpen}
                    onClose={() => setIsStopConfirmOpen(false)}
                    onConfirm={() => {
                        stopMutation.mutate(projectId, {
                            onSuccess: () => setIsStopConfirmOpen(false)
                        });
                    }}
                    title="Abort Intelligence Scan?"
                    message="Stopping the scan will immediately terminate all discovery and processing threads. Existing progress will be discarded for this session."
                    confirmText="Stop Anyway"
                    variant="danger"
                    isLoading={stopMutation.isPending}
                />
            </div>
        </AnimatePresence>
    );
}
