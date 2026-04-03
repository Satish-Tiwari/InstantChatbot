import Link from 'next/link';
import { Navbar } from '@/components/layout/Navbar';
import {
  Globe, Brain, Package, Zap, Palette, Shield,
  ArrowRight, ChevronRight,
} from 'lucide-react';

const FEATURES = [
  {
    icon: Globe,
    title: 'Smart Crawling',
    desc: 'Automatically crawls your website following links and sitemaps, extracting all meaningful content.',
  },
  {
    icon: Brain,
    title: 'RAG-Powered AI',
    desc: 'Uses Retrieval-Augmented Generation to deliver accurate, context-aware answers from your content.',
  },
  {
    icon: Package,
    title: 'Ready-to-Deploy',
    desc: 'Download a complete chatbot package — server, widget, docs, and deployment scripts included.',
  },
  {
    icon: Zap,
    title: 'Under 10 Minutes',
    desc: 'From URL input to a working chatbot in minutes. No training data preparation required.',
  },
  {
    icon: Palette,
    title: 'Embeddable Widget',
    desc: 'Beautiful, customizable chat widget you can embed on any website with a single script tag.',
  },
  {
    icon: Shield,
    title: 'Secure & Isolated',
    desc: 'Each project has its own isolated knowledge base. Your data stays private and secure.',
  },
];

const STEPS = [
  { num: '01', title: 'Enter URL', desc: 'Paste your website URL' },
  { num: '02', title: 'We Crawl & Process', desc: 'Content is extracted and cleaned' },
  { num: '03', title: 'AI Learns', desc: 'Embeddings generated for RAG' },
  { num: '04', title: 'Download & Deploy', desc: 'Get your chatbot ZIP package' },
];

export default function HomePage() {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />

      {/* Hero */}
      <section className="relative z-10 text-center pt-32 pb-16 px-6">
        <div className="max-w-4xl mx-auto animate-fade-in">
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full
                          bg-brand-500/10 text-brand-400 text-sm font-medium mb-8
                          border border-brand-500/20">
            <Zap className="w-3.5 h-3.5" />
            Powered by Spring AI + OpenAI
          </div>

          <h1 className="text-5xl md:text-7xl font-black mb-6 leading-tight
                         bg-gradient-to-br from-white via-brand-300 to-purple-400
                         bg-clip-text text-transparent">
            Transform Any Website
            <br />
            Into an AI Chatbot
          </h1>

          <p className="text-lg md:text-xl text-gray-400 max-w-2xl mx-auto mb-10 leading-relaxed">
            Enter a URL, and we&apos;ll crawl, process, and generate a fully functional
            AI-powered chatbot trained on the website&apos;s content — ready to deploy in minutes.
          </p>

          <div className="flex gap-4 justify-center">
            <Link href="/register" className="btn-brand !py-4 !px-8 text-base">
              Get Started Free <ArrowRight className="w-4 h-4" />
            </Link>
            <Link href="/login" className="btn-ghost !py-4 !px-8 text-base">
              Sign In <ChevronRight className="w-4 h-4" />
            </Link>
          </div>
        </div>

        {/* Mock terminal */}
        <div className="max-w-2xl mx-auto mt-16 animate-float">
          <div className="glass p-6 text-left">
            <div className="flex gap-2 mb-4">
              <div className="w-3 h-3 rounded-full bg-red-400/80" />
              <div className="w-3 h-3 rounded-full bg-amber-400/80" />
              <div className="w-3 h-3 rounded-full bg-emerald-400/80" />
            </div>
            <div className="flex items-center gap-3 p-3 rounded-xl bg-surface-100 border border-white/[0.06] mb-3">
              <Globe className="w-4 h-4 text-gray-500" />
              <span className="text-brand-400 font-mono text-sm">https://your-website.com</span>
              <span className="ml-auto bg-gradient-brand px-4 py-1.5 rounded-lg text-xs font-semibold text-white">
                Generate →
              </span>
            </div>
            <div className="flex gap-4 text-xs text-gray-500">
              <span className="text-emerald-400">✓ 47 pages crawled</span>
              <span>•</span>
              <span className="text-blue-400">📊 156 chunks</span>
              <span>•</span>
              <span className="text-brand-400">🤖 Chatbot ready</span>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="relative z-10 py-24 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">Why Instant Chatbot?</h2>
            <p className="text-gray-400 max-w-lg mx-auto">
              Everything you need to create, deploy, and manage AI chatbots for any website.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {FEATURES.map((f, i) => (
              <div
                key={i}
                className="glass glass-hover p-7 text-center animate-fade-in"
                style={{ animationDelay: `${i * 80}ms` }}
              >
                <div className="w-12 h-12 rounded-xl bg-brand-500/10 flex items-center justify-center
                                mx-auto mb-4 text-brand-400">
                  <f.icon className="w-6 h-6" />
                </div>
                <h3 className="text-lg font-semibold mb-2">{f.title}</h3>
                <p className="text-sm text-gray-400 leading-relaxed">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Steps */}
      <section className="relative z-10 py-24 px-6">
        <div className="max-w-5xl mx-auto">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-16">How It Works</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-5">
            {STEPS.map((step, i) => (
              <div
                key={i}
                className="glass glass-hover p-6 text-center animate-fade-in"
                style={{ animationDelay: `${i * 120}ms` }}
              >
                <div className="w-12 h-12 rounded-full bg-gradient-brand flex items-center justify-center
                                mx-auto mb-4 text-white font-bold text-lg">
                  {step.num}
                </div>
                <h4 className="font-semibold mb-1">{step.title}</h4>
                <p className="text-xs text-gray-500">{step.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="relative z-10 py-24 px-6 text-center">
        <h2 className="text-3xl font-bold mb-4">Ready to Build Your Chatbot?</h2>
        <p className="text-gray-400 mb-8">Start for free. No credit card required.</p>
        <Link href="/register" className="btn-brand !py-4 !px-10 text-base">
          Get Started Now <ArrowRight className="w-4 h-4" />
        </Link>
      </section>

      {/* Footer */}
      <footer className="relative z-10 border-t border-white/[0.06] py-8 text-center">
        <p className="text-sm text-gray-500">
          © 2024 Instant Chatbot. Built with ❤️ using Spring AI, Next.js, and OpenAI.
        </p>
      </footer>
    </div>
  );
}
