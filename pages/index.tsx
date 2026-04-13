import { useState } from 'react';
import Head from 'next/head';
import { Download, Clock, Phone, Activity, Hospital, Smartphone } from 'lucide-react';

export default function Home() {
  const [downloadingMSI, setDownloadingMSI] = useState(false);
  const [downloadingAPK, setDownloadingAPK] = useState(false);

  const handleDownloadMSI = () => {
    setDownloadingMSI(true);
    setTimeout(() => {
      const url = "https://github.com/Ammar-flynn/Ambulancer/releases/download/v1.0.0/Ambulancer-1.0.msi";
      const a = document.createElement("a");
      a.href = url;
      a.download = "Ambulancer-1.0.msi";
      document.body.appendChild(a);
      a.click();
      a.remove();
      setDownloadingMSI(false);
    }, 1000);
  };

  const handleDownloadAPK = () => {
    setDownloadingAPK(true);
    setTimeout(() => {
      const url = "https://github.com/Ammar-flynn/Ambulancer/releases/download/v1.0.0/Ambulancer.apk";
      const a = document.createElement("a");
      a.href = url;
      a.download = "Ambulancer-1.0.apk";
      document.body.appendChild(a);
      a.click();
      a.remove();
      setDownloadingAPK(false);
    }, 1000);
  };

  return (
    <>
      <Head>
        <title>Ambulancer | Emergency Dispatch System</title>
      </Head>

      <div className="landing-page">
        <header className="landing-header">
          <div className="landing-header-content">
            <div className="logo-container">
              <img id="logo" src="/AmbulancerIcon.png" alt="Ambulancer Logo" />
              <h1 id="Title">Ambulancer</h1>
            </div>
            <div className="header-buttons">
              <button className="download-btn-apk" onClick={handleDownloadAPK} disabled={downloadingAPK}>
                <Smartphone size={18} />
                {downloadingAPK ? 'Preparing...' : 'Download APK'}
              </button>
              <button className="download-btn" onClick={handleDownloadMSI} disabled={downloadingMSI}>
                <Download size={18} />
                {downloadingMSI ? 'Preparing...' : 'Download MSI'}
              </button>
            </div>
          </div>
        </header>

        <section className="landing-hero">
          <h1>Emergency Dispatch <span>Made Simple</span></h1>
          <p>Emergency Reports, Emergency Responding, Dispatch System</p>
          <div className="hero-buttons">
            <button className="hero-download-btn" onClick={handleDownloadMSI} disabled={downloadingMSI}>
              <Download size={20} />
              {downloadingMSI ? 'Preparing...' : 'Download Ambulancer MSI'}
            </button>
            <button className="hero-download-btn-apk" onClick={handleDownloadAPK} disabled={downloadingAPK}>
              <Smartphone size={20} />
              {downloadingAPK ? 'Preparing...' : 'Download APK'}
            </button>
          </div>
          <div className="hero-stats">
            <div><span>40%</span> Faster Response</div>
            <div><span>500+</span> Agencies</div>
            <div><span>24/7</span> Support</div>
          </div>
        </section>

        <section id="features" className="landing-features">
          <h2>Key Features</h2>
          <div className="features-grid">
            <div><Hospital size={32} /> Hospital Lists</div>
            <div><Clock size={32} /> Quick Responses</div>
            <div><Phone size={32} /> 911 Integration</div>
            <div><Activity size={32} /> Request Dashboard</div>
          </div>
        </section>

        <section id="download" className="landing-download">
          <h2>Ready to save lives?</h2>
          <div className="download-options">
            <button className="download-large-btn" onClick={handleDownloadMSI} disabled={downloadingMSI}>
              <Download size={24} />
              {downloadingMSI ? 'Preparing...' : 'Download MSI Installer'}
            </button>
            <button className="download-large-btn-apk" onClick={handleDownloadAPK} disabled={downloadingAPK}>
              <Smartphone size={24} />
              {downloadingAPK ? 'Preparing...' : 'Download APK'}
            </button>
          </div>
          <p>Windows 10/11 | Android 8.0+ | Version 1.0</p>
        </section>

        <footer className="landing-footer">
          <p>© 2026 Ambulancer. All rights reserved.</p>
        </footer>
      </div>
    </>
  );
}