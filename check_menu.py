from playwright.sync_api import sync_playwright
with sync_playwright() as p:
    b=p.chromium.launch(channel="msedge",headless=True)
    for w in [1440,1919,1536,1100,1080,1000]:
        page=b.new_page(viewport={"width":w,"height":900})
        page.goto("http://localhost:18094/login", wait_until="domcontentloaded")
        # login screen has no menu; use direct html via set_content? go login then auth not needed CSS
        print(w, page.evaluate("""() => { const el=document.querySelector('.menu-toggle'); return el ? [getComputedStyle(el).display, innerWidth] : 'no el'; }"""))
        page.close()
    b.close()
