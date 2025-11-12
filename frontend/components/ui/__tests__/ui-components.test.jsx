import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { Button } from '../button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../card';

describe('Button Component', () => {
  it('renders children correctly', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('calls onClick handler when clicked', () => {
    const handleClick = jest.fn();
    render(<Button onClick={handleClick}>Click me</Button>);
    
    fireEvent.click(screen.getByText('Click me'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('applies correct type attribute', () => {
    render(<Button type="submit">Submit</Button>);
    expect(screen.getByText('Submit')).toHaveAttribute('type', 'submit');
  });

  it('defaults to button type when not specified', () => {
    render(<Button>Default</Button>);
    expect(screen.getByText('Default')).toHaveAttribute('type', 'button');
  });

  it('applies custom className', () => {
    render(<Button className="custom-class">Styled</Button>);
    expect(screen.getByText('Styled')).toHaveClass('custom-class');
  });

  it('changes style on hover', () => {
    render(<Button>Hover me</Button>);
    const button = screen.getByText('Hover me');
    
    // Initial state
    const initialBg = button.style.backgroundColor;
    
    // Hover
    fireEvent.mouseEnter(button);
    const hoverBg = button.style.backgroundColor;
    
    // Should change on hover
    expect(hoverBg).not.toBe(initialBg);
    
    // Mouse leave
    fireEvent.mouseLeave(button);
    expect(button.style.backgroundColor).toBe(initialBg);
  });


});

describe('Card Components', () => {
  describe('Card', () => {
    it('renders children correctly', () => {
      render(<Card>Card content</Card>);
      expect(screen.getByText('Card content')).toBeInTheDocument();
    });

    it('applies custom className', () => {
      const { container } = render(<Card className="custom-card">Content</Card>);
      expect(container.firstChild).toHaveClass('custom-card');
    });
  });

  describe('CardHeader', () => {
    it('renders children correctly', () => {
      render(<CardHeader>Header content</CardHeader>);
      expect(screen.getByText('Header content')).toBeInTheDocument();
    });

    it('applies custom className', () => {
      render(<CardHeader className="custom-header">Header</CardHeader>);
      expect(screen.getByText('Header')).toHaveClass('custom-header');
    });
  });

  describe('CardTitle', () => {
    it('renders as h3 element', () => {
      render(<CardTitle>Title</CardTitle>);
      const title = screen.getByText('Title');
      expect(title.tagName).toBe('H3');
    });

    it('applies correct styling classes', () => {
      render(<CardTitle>Title</CardTitle>);
      const title = screen.getByText('Title');
      expect(title).toHaveClass('text-lg', 'font-bold', 'mb-2');
    });
  });

  describe('CardDescription', () => {
    it('renders as paragraph element', () => {
      render(<CardDescription>Description text</CardDescription>);
      const desc = screen.getByText('Description text');
      expect(desc.tagName).toBe('P');
    });

    it('applies custom className', () => {
      render(<CardDescription className="custom-desc">Description</CardDescription>);
      expect(screen.getByText('Description')).toHaveClass('custom-desc');
    });
  });

  describe('CardContent', () => {
    it('renders children correctly', () => {
      render(<CardContent>Content text</CardContent>);
      expect(screen.getByText('Content text')).toBeInTheDocument();
    });

    it('applies custom className', () => {
      render(<CardContent className="custom-content">Content</CardContent>);
      expect(screen.getByText('Content')).toHaveClass('custom-content');
    });
  });

  describe('Card with all components', () => {
    it('renders complete card structure', () => {
      render(
        <Card>
          <CardHeader>
            <CardTitle>Test Card</CardTitle>
            <CardDescription>This is a test card</CardDescription>
          </CardHeader>
          <CardContent>
            <p>Card body content</p>
          </CardContent>
        </Card>
      );

      expect(screen.getByText('Test Card')).toBeInTheDocument();
      expect(screen.getByText('This is a test card')).toBeInTheDocument();
      expect(screen.getByText('Card body content')).toBeInTheDocument();
    });
  });
});
