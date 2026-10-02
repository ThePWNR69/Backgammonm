#!/usr/bin/env python3
"""Rebuild the default Classic Wood checker sprites with identical LIGHT/DARK geometry."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter
import numpy as np
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'app/src/main/assets/cosmetics/checkers/ivory_walnut'

def wood(w,h,base,dark,light,seed):
    rng=np.random.default_rng(seed); y,x=np.mgrid[0:h,0:w]
    coarse=np.sin(y*.018+np.sin(x*.007)*1.5)*.45+np.sin(y*.041+x*.004)*.18
    noise=rng.normal(0,1,(h,w)); n=Image.fromarray(np.uint8(np.clip((noise+3)*42,0,255))).filter(ImageFilter.GaussianBlur(max(1,w/180)))
    v=np.clip(coarse*.13+(np.asarray(n,dtype=np.float32)-126)/126*.06,-.22,.22)
    b=np.array(base,float); d=np.array(dark,float); l=np.array(light,float)
    rgb=b+np.maximum(v,0)[...,None]*(l-b)*1.7+np.maximum(-v,0)[...,None]*(d-b)*1.45
    return Image.fromarray(np.uint8(np.clip(rgb,0,255)),'RGB')

def top(path,p,seed):
    S=1024; tex=wood(S,S,*p,seed); out=Image.new('RGBA',(S,S),(0,0,0,0)); box=(64,64,960,960)
    mask=Image.new('L',(S,S),0); ImageDraw.Draw(mask).ellipse(box,fill=255); out.paste(tex,(0,0),mask); d=ImageDraw.Draw(out)
    d.ellipse(box,outline=p[1]+(255,),width=18); d.ellipse((80,80,944,944),outline=p[2]+(210,),width=9)
    d.ellipse((144,144,880,880),outline=p[1]+(115,),width=10); d.ellipse((158,158,866,866),outline=p[2]+(75,),width=5)
    hi=Image.new('RGBA',(S,S),(0,0,0,0)); ImageDraw.Draw(hi).arc((82,82,942,942),205,328,fill=(255,255,255,95),width=15)
    out=Image.alpha_composite(out,hi.filter(ImageFilter.GaussianBlur(3))).resize((512,512),Image.Resampling.LANCZOS); out.save(path)

def side(path,p,seed):
    W,H=1024,256; tex=wood(W,H,*p,seed); out=Image.new('RGBA',(W,H),(0,0,0,0)); box=(64,42,960,214)
    mask=Image.new('L',(W,H),0); ImageDraw.Draw(mask).rounded_rectangle(box,radius=70,fill=255); out.paste(tex,(0,0),mask); d=ImageDraw.Draw(out)
    d.rounded_rectangle(box,radius=70,outline=p[1]+(255,),width=14); d.line((92,63,932,63),fill=p[2]+(180,),width=7); d.line((92,194,932,194),fill=p[1]+(150,),width=7); d.line((110,128,914,128),fill=p[1]+(80,),width=4)
    out.resize((512,128),Image.Resampling.LANCZOS).save(path)

light=((219,174,112),(155,103,51),(248,218,168)); dark=((105,56,28),(47,22,12),(166,103,62))
top(OUT/'light.png',light,11); top(OUT/'dark.png',dark,21); side(OUT/'light_side.png',light,31); side(OUT/'dark_side.png',dark,41)
print('Rebuilt Classic Wood top-down + side-view checker assets.')
